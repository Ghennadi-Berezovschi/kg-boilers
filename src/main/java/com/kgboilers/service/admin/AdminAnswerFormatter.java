package com.kgboilers.service.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.kgboilers.model.admin.AdminAnswerSection;
import com.kgboilers.model.admin.AdminDetailRow;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns saved answers into readable sections without changing the stored quote. */
final class AdminAnswerFormatter {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("fuel", "Fuel type"),
            Map.entry("ownership", "Property ownership"),
            Map.entry("bedrooms", "Bedrooms"),
            Map.entry("bathShowerCount", "Baths and showers"),
            Map.entry("hotWaterAvailable", "Hot water available"),
            Map.entry("boilerMake", "Boiler brand"),
            Map.entry("boilerFloorLevel", "Floor level"),
            Map.entry("powerFlushStatus", "Power flushing"),
            Map.entry("magneticFilterStatus", "Magnetic filter"),
            Map.entry("trvValveStatus", "TRV valves"),
            Map.entry("targetType", "Recommended boiler type"),
            Map.entry("exactMatch", "Exact match available"),
            Map.entry("powerKw", "Output"),
            Map.entry("averagePriceGbp", "Boiler price"),
            Map.entry("standardInstallationGbp", "Standard installation"),
            Map.entry("standardItemsTotalGbp", "Included materials"),
            Map.entry("totalPriceGbp", "Total price"),
            Map.entry("installationPriceGbp", "Installation quote total"),
            Map.entry("optionalExtrasPriceGbp", "Optional extras total"),
            Map.entry("priceGbp", "Price"),
            Map.entry("relocationPriceGbp", "Boiler relocation"),
            Map.entry("flueLengthPriceGbp", "Additional flue length"),
            Map.entry("fluePositionPriceGbp", "Flue position supplement"),
            Map.entry("flueClearancePriceGbp", "Flue clearance supplement"),
            Map.entry("horizontalFlueShapePriceGbp", "Flue shape supplement"),
            Map.entry("heatOnlyConversionPriceGbp", "Heat-only conversion"),
            Map.entry("trvValves", "TRV valves"),
            Map.entry("lengthMm", "Length"),
            Map.entry("widthMm", "Width")
    );

    private AdminAnswerFormatter() {
    }

    static List<AdminAnswerSection> format(JsonNode root) {
        Map<String, List<AdminDetailRow>> sections = new LinkedHashMap<>();
        if (root == null || root.isNull()) {
            return List.of();
        }
        collect(root, "", "Request details", sections);
        return sections.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new AdminAnswerSection(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    private static void collect(JsonNode node, String key, String section,
                                Map<String, List<AdminDetailRow>> sections) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(field -> {
                String name = field.getKey();
                // These have dedicated views, or are internal catalogue metadata.
                if (List.of("uploadedPictures", "jobPictures", "selectedOptionalExtras", "image", "id").contains(name)) {
                    return;
                }
                String nextSection = switch (name) {
                    case "answers" -> section;
                    case "pricing" -> "Price breakdown";
                    case "recommendation" -> "Recommendation details";
                    case "valveQuantities" -> "Valve quantities";
                    default -> (key.isBlank() && "Request details".equals(section)) || "answers".equals(key)
                            ? category(name) : section;
                };
                String labelKey = nodeDepthLabel(key, name);
                collect(field.getValue(), labelKey, nextSection, sections);
            });
            return;
        }
        if (node.isArray()) {
            boolean structured = false;
            for (JsonNode item : node) {
                structured |= item.isContainerNode();
            }
            if (structured) {
                int index = 0;
                for (JsonNode item : node) {
                    index++;
                    String itemSection = switch (key) {
                        case "boilers" -> "Boiler option " + index;
                        case "installationItems" -> "Installation item " + index;
                        default -> humanize(key) + " · " + index;
                    };
                    collect(item, "", itemSection, sections);
                }
                return;
            }
            List<String> values = new ArrayList<>();
            node.forEach(item -> {
                if (!item.isNull() && !item.asText().isBlank()) {
                    values.add(displayValue(key, item));
                }
            });
            if (!values.isEmpty()) {
                add(sections, section, label(key), String.join(", ", values));
            }
            return;
        }
        if (!node.asText().isBlank()) {
            add(sections, section, label(key), displayValue(key, node));
        }
    }

    private static String nodeDepthLabel(String parent, String name) {
        // Keep context for unknown nested fields without repeating known section names.
        if (parent.isBlank() || List.of("answers", "pricing", "recommendation", "valveQuantities").contains(parent)) {
            return name;
        }
        return parent + "." + name;
    }

    private static String category(String key) {
        return switch (key) {
            case "postcode", "ownership", "propertyType", "bedrooms", "bathShowerCount", "radiatorCount" -> "Property & household";
            case "boilerLocation", "boilerPosition", "boilerFloorLevel", "relocation", "relocationDistance",
                    "flueType", "horizontalFlueShape", "verticalFlueType", "flueLength", "slopedRoofPosition",
                    "fluePosition", "flueClearance", "fluePropertyDistance" -> "Location & flue";
            case "fuel", "boilerType", "boilerMake", "boilerAge", "boilerCondition", "hotWaterAvailable" -> "Boiler & fuel";
            case "powerFlushStatus", "magneticFilterStatus", "trvValveStatus", "radiatorIssues", "otherRadiatorIssueDetails",
                    "repairProblem", "boilerPressureStatus", "faultCodeDisplayStatus", "faultCodeDetails", "problemDetails" -> "Heating & repair";
            default -> "Request details";
        };
    }

    private static String displayValue(String key, JsonNode node) {
        if (node.isBoolean()) {
            return node.asBoolean() ? "Yes" : "No";
        }
        if (node.isNumber()) {
            if (key.endsWith("Gbp")) {
                NumberFormat money = NumberFormat.getCurrencyInstance(Locale.UK);
                money.setMinimumFractionDigits(0);
                money.setMaximumFractionDigits(2);
                return money.format(node.decimalValue());
            }
            if (key.endsWith("Kw")) {
                return node.asText() + " kW";
            }
            if (key.endsWith("Mm")) {
                return node.asText() + " mm";
            }
        }
        String value = node.asText();
        if ("serviceType".equals(key)) {
            return humanize(value);
        }
        if (value.matches("[A-Z][A-Z0-9_]*")) {
            return switch (value) {
                case "LPG", "UK", "CP12" -> value;
                default -> humanize(value);
            };
        }
        return value;
    }

    private static String label(String key) {
        return LABELS.getOrDefault(key, humanize(key));
    }

    private static String humanize(String value) {
        String text = value.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("[_.-]", " ").trim().toLowerCase(Locale.UK);
        return text.isBlank() ? "Answer" : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static void add(Map<String, List<AdminDetailRow>> sections, String section, String label, String value) {
        sections.computeIfAbsent(section, ignored -> new ArrayList<>()).add(new AdminDetailRow(label, value));
    }
}
