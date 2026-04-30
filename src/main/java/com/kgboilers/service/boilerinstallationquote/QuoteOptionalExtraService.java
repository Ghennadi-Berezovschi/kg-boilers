package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuoteOptionalExtraService {

    private static final int MAX_REPEATABLE_QUANTITY = 9;

    private final QuoteOfferProperties quoteOfferProperties;

    public QuoteOptionalExtraService(QuoteOfferProperties quoteOfferProperties) {
        this.quoteOfferProperties = quoteOfferProperties;
    }

    public List<QuoteOptionalExtra> getAllOptionalExtras() {
        return quoteOfferProperties.getOptionalExtras();
    }

    public List<QuoteOptionalExtra> getOptionalExtrasFor(BoilerType boilerType) {
        return quoteOfferProperties.getOptionalExtras().stream()
                .filter(extra -> appliesToBoilerType(extra, boilerType))
                .toList();
    }

    public List<QuoteOptionalExtra> resolveSelectedExtras(List<String> selectedExtraIds, BoilerType boilerType) {
        if (selectedExtraIds == null || selectedExtraIds.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> selectedCounts = selectedExtraIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(
                        LinkedHashMap::new,
                        (counts, id) -> counts.merge(id, 1, Integer::sum),
                        (left, right) -> right.forEach((id, count) -> left.merge(id, count, Integer::sum))
                );
        Set<String> ids = new LinkedHashSet<>(selectedCounts.keySet());
        return quoteOfferProperties.getOptionalExtras().stream()
                .filter(extra -> appliesToBoilerType(extra, boilerType))
                .filter(extra -> extra.getId() != null && ids.contains(extra.getId()))
                .map(extra -> withSelectedQuantity(extra, selectedCounts.getOrDefault(extra.getId(), 1)))
                .toList();
    }

    public List<QuoteOptionalExtra> resolveSelectedExtras(List<String> selectedExtraIds) {
        return resolveSelectedExtras(selectedExtraIds, null);
    }

    public int getTotalPriceGbp(List<QuoteOptionalExtra> selectedExtras) {
        if (selectedExtras == null || selectedExtras.isEmpty()) {
            return 0;
        }

        return selectedExtras.stream()
                .map(QuoteOptionalExtra::getPriceGbp)
                .filter(price -> price != null && price > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private boolean appliesToBoilerType(QuoteOptionalExtra extra, BoilerType boilerType) {
        if (extra == null) {
            return false;
        }

        List<String> appliesToBoilerTypes = extra.getAppliesToBoilerTypes();
        if (appliesToBoilerTypes == null || appliesToBoilerTypes.isEmpty()) {
            return boilerType != BoilerType.HEAT_ONLY;
        }

        if (boilerType == null) {
            return false;
        }

        String boilerTypeValue = boilerType.getValue();
        String boilerTypeName = boilerType.name();
        return appliesToBoilerTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .anyMatch(value -> value.equalsIgnoreCase(boilerTypeValue) || value.equalsIgnoreCase(boilerTypeName));
    }

    private QuoteOptionalExtra withSelectedQuantity(QuoteOptionalExtra extra, int requestedQuantity) {
        int quantity = extra.isRepeatable() ? Math.max(1, Math.min(MAX_REPEATABLE_QUANTITY, requestedQuantity)) : 1;
        QuoteOptionalExtra selectedExtra = new QuoteOptionalExtra();
        selectedExtra.setId(extra.getId());
        selectedExtra.setTitle(extra.getTitle());
        selectedExtra.setDescription(extra.getDescription());
        selectedExtra.setImage(extra.getImage());
        selectedExtra.setRepeatable(extra.isRepeatable());
        selectedExtra.setQuantity(quantity);
        selectedExtra.setAppliesToBoilerTypes(extra.getAppliesToBoilerTypes());
        selectedExtra.setPriceGbp(extra.getPriceGbp() == null ? null : extra.getPriceGbp() * quantity);
        return selectedExtra;
    }
}
