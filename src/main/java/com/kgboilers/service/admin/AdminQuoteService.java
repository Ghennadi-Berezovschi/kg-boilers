package com.kgboilers.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kgboilers.model.admin.AdminDetailRow;
import com.kgboilers.model.admin.AdminQuoteDetail;
import com.kgboilers.model.admin.AdminQuoteFilters;
import com.kgboilers.model.admin.AdminQuoteExtra;
import com.kgboilers.model.admin.AdminQuoteListItem;
import com.kgboilers.model.admin.AdminUploadedPicture;
import com.kgboilers.model.boilerinstallationquote.UploadedPicture;
import com.kgboilers.service.boilerinstallationquote.QuotePictureStorageService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminQuoteService {

    private static final ZoneId LONDON_ZONE = ZoneId.of("Europe/London");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.UK);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final QuotePictureStorageService quotePictureStorageService;

    public AdminQuoteService(NamedParameterJdbcTemplate jdbcTemplate,
                             ObjectMapper objectMapper,
                             QuotePictureStorageService quotePictureStorageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.quotePictureStorageService = quotePictureStorageService;
    }

    public List<AdminQuoteListItem> findAll() {
        return findAll(new AdminQuoteFilters(null, null, null, null, null));
    }

    public List<AdminQuoteListItem> findAll(AdminQuoteFilters filters) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       created_at,
                       status,
                       service_type,
                       client_name,
                       client_email,
                       client_phone,
                       postcode,
                       selected_boiler,
                       recommended_boiler,
                       installation_price_gbp,
                       optional_extras_price_gbp
                FROM quotes
                WHERE 1 = 1
                """);

        appendFilters(sql, params, filters);
        sql.append(" ORDER BY created_at DESC, id DESC");

        return jdbcTemplate.query(sql.toString(), params, new QuoteListRowMapper());
    }

    public List<String> serviceOptions() {
        return List.of(
                "boiler-installation",
                "boiler-repair",
                "central-heating",
                "gas-safety-certificate",
                "hot-water-cylinder"
        );
    }

    public List<String> statusOptions() {
        return List.of("NEW_LEAD", "CONTACTED", "COMPLETED", "CANCELLED");
    }

    public Optional<AdminQuoteDetail> findById(long id) {
        List<AdminQuoteDetail> results = jdbcTemplate.query("""
                SELECT id,
                       created_at,
                       updated_at,
                       contact_requested_at,
                       status,
                       service_type,
                       client_name,
                       client_email,
                       client_phone,
                       postcode,
                       selected_boiler,
                       recommended_boiler,
                       installation_price_gbp,
                       optional_extras_price_gbp,
                       selected_optional_extras_json::text AS selected_optional_extras_json,
                       client_answers_json::text AS client_answers_json
                FROM quotes
                WHERE id = :id
                """, new MapSqlParameterSource("id", id), new QuoteDetailRowMapper());
        return results.stream().findFirst();
    }

    public void updateStatus(long id, String status) {
        if (!List.of("NEW_LEAD", "CONTACTED", "COMPLETED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("Unsupported quote status: " + status);
        }

        jdbcTemplate.update("""
                UPDATE quotes
                SET status = :status,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status));
    }

    private void appendFilters(StringBuilder sql, MapSqlParameterSource params, AdminQuoteFilters filters) {
        if (filters == null) {
            return;
        }

        if (hasText(filters.query())) {
            sql.append("""
                     AND (
                         CAST(id AS text) ILIKE :query
                         OR client_name ILIKE :query
                         OR client_email ILIKE :query
                         OR client_phone ILIKE :query
                         OR postcode ILIKE :query
                         OR selected_boiler ILIKE :query
                         OR recommended_boiler ILIKE :query
                         OR service_type ILIKE :query
                     )
                    """);
            params.addValue("query", "%" + filters.query().trim() + "%");
        }

        if (hasText(filters.serviceType())) {
            sql.append(" AND replace(lower(service_type), '_', '-') = :serviceType");
            params.addValue("serviceType", normalizeServiceFilter(filters.serviceType()));
        }

        if (hasText(filters.status())) {
            sql.append(" AND status = :status");
            params.addValue("status", filters.status().trim().toUpperCase(Locale.UK));
        }

        if (filters.dateFrom() != null) {
            sql.append(" AND created_at >= :dateFrom");
            params.addValue("dateFrom", filters.dateFrom().atStartOfDay());
        }

        if (filters.dateTo() != null) {
            sql.append(" AND created_at < :dateTo");
            params.addValue("dateTo", filters.dateTo().plusDays(1).atStartOfDay());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeServiceFilter(String serviceType) {
        return serviceType.trim().toLowerCase(Locale.UK).replace('_', '-');
    }

    private final class QuoteListRowMapper implements RowMapper<AdminQuoteListItem> {

        @Override
        public AdminQuoteListItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminQuoteListItem(
                    rs.getLong("id"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                    rs.getString("status"),
                    displayServiceType(rs.getString("service_type")),
                    rs.getString("client_name"),
                    rs.getString("client_email"),
                    rs.getString("client_phone"),
                    rs.getString("postcode"),
                    firstNonBlank(rs.getString("selected_boiler"), rs.getString("recommended_boiler")),
                    totalPrice(rs)
            );
        }
    }

    private final class QuoteDetailRowMapper implements RowMapper<AdminQuoteDetail> {

        @Override
        public AdminQuoteDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminQuoteDetail(
                    rs.getLong("id"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                    formatTimestamp(rs.getTimestamp("updated_at")),
                    formatTimestamp(rs.getTimestamp("contact_requested_at")),
                    rs.getString("status"),
                    displayServiceType(rs.getString("service_type")),
                    rs.getString("client_name"),
                    rs.getString("client_email"),
                    rs.getString("client_phone"),
                    rs.getString("postcode"),
                    firstNonBlank(rs.getString("selected_boiler"), rs.getString("recommended_boiler")),
                    totalPrice(rs),
                    parseExtras(rs.getString("selected_optional_extras_json")),
                    parsePictures(rs.getString("client_answers_json"), "uploadedPictures"),
                    parsePictures(rs.getString("client_answers_json"), "jobPictures"),
                    parseAnswers(rs.getString("client_answers_json"))
            );
        }
    }

    @Transactional
    public int addJobPictures(long id, List<MultipartFile> pictures) {
        List<UploadedPicture> uploadedPictures = quotePictureStorageService.storePictures(pictures);
        if (uploadedPictures.isEmpty()) {
            throw new IllegalArgumentException("Please choose at least one picture.");
        }

        String clientAnswersJson = jdbcTemplate.queryForObject("""
                SELECT client_answers_json::text
                FROM quotes
                WHERE id = :id
                """, new MapSqlParameterSource("id", id), String.class);

        ObjectNode root = readAnswersRoot(clientAnswersJson);
        ArrayNode jobPictures = root.withArray("jobPictures");
        for (UploadedPicture picture : uploadedPictures) {
            jobPictures.add(objectMapper.valueToTree(picture));
        }

        jdbcTemplate.update("""
                UPDATE quotes
                SET client_answers_json = CAST(:clientAnswersJson AS jsonb),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("clientAnswersJson", root.toString()));

        return uploadedPictures.size();
    }

    private ObjectNode readAnswersRoot(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            if (root instanceof ObjectNode objectNode) {
                return objectNode;
            }
        } catch (Exception ignored) {
            // Fall through and keep the quote usable with a fresh JSON object.
        }

        return objectMapper.createObjectNode();
    }

    private Integer totalPrice(ResultSet rs) throws SQLException {
        Integer installationPrice = nullableInt(rs, "installation_price_gbp");
        Integer extrasPrice = nullableInt(rs, "optional_extras_price_gbp");
        if (installationPrice != null) {
            return installationPrice;
        }
        return extrasPrice != null && extrasPrice > 0 ? extrasPrice : null;
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private List<AdminQuoteExtra> parseExtras(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }

            List<AdminQuoteExtra> extras = new ArrayList<>();
            for (JsonNode item : root) {
                extras.add(new AdminQuoteExtra(
                        textValue(item, "title"),
                        intValue(item, "quantity"),
                        intValue(item, "priceGbp")
                ));
            }
            return extras;
        } catch (Exception ex) {
            return List.of(new AdminQuoteExtra("Could not read selected extras", null, null));
        }
    }

    private List<AdminDetailRow> parseAnswers(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            List<AdminDetailRow> rows = new ArrayList<>();
            flattenJson("", objectMapper.readTree(json), rows);
            return rows.stream()
                    .filter(row -> row.value() != null && !row.value().isBlank() && !"null".equals(row.value()))
                    .toList();
        } catch (Exception ex) {
            return List.of(new AdminDetailRow("Answers", "Could not read saved answers"));
        }
    }

    private void flattenJson(String prefix, JsonNode node, List<AdminDetailRow> rows) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (prefix.isBlank() && ("uploadedPictures".equals(field.getKey()) || "jobPictures".equals(field.getKey()))) {
                    continue;
                }
                String nextPrefix = prefix.isBlank() ? field.getKey() : prefix + "." + field.getKey();
                flattenJson(nextPrefix, field.getValue(), rows);
            }
            return;
        }

        rows.add(new AdminDetailRow(humanize(prefix), displayJsonValue(node)));
    }

    private String displayJsonValue(JsonNode node) {
        if (node.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                values.add(displayJsonValue(item));
            }
            return String.join(", ", values);
        }
        if (node.isObject()) {
            return node.toString();
        }
        return node.asText();
    }

    private List<AdminUploadedPicture> parsePictures(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonNode pictures = objectMapper.readTree(json).get(fieldName);
            if (pictures == null || !pictures.isArray()) {
                return List.of();
            }

            List<AdminUploadedPicture> uploadedPictures = new ArrayList<>();
            for (JsonNode picture : pictures) {
                String url = textValue(picture, "url");
                if (url.isBlank()) {
                    continue;
                }
                uploadedPictures.add(new AdminUploadedPicture(
                        firstNonBlank(textValue(picture, "originalFilename"), "Picture"),
                        url
                ));
            }
            return uploadedPictures;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private Integer intValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || !value.isNumber() ? null : value.asInt();
    }

    private String displayServiceType(String serviceType) {
        if (serviceType == null || serviceType.isBlank()) {
            return "Unknown service";
        }
        return switch (serviceType) {
            case "BOILER_INSTALLATION" -> "Boiler Installation";
            case "BOILER_REPAIR" -> "Boiler Repair";
            case "CENTRAL_HEATING" -> "Central Heating";
            case "GAS_SAFETY_CERTIFICATE" -> "Boiler Service and Gas Safety Certificate";
            default -> humanize(serviceType.toLowerCase(Locale.UK));
        };
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : "Not selected";
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "Not sent yet";
        }
        return timestamp.toInstant().atZone(LONDON_ZONE).format(DATE_FORMATTER);
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String spaced = value
                .replace(".", " ")
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim();
        if (spaced.isBlank()) {
            return "";
        }
        return spaced.substring(0, 1).toUpperCase(Locale.UK) + spaced.substring(1);
    }
}
