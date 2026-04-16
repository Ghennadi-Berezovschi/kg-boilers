package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.QuoteOfferProperties;
import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuoteOptionalExtraService {

    private final QuoteOfferProperties quoteOfferProperties;

    public QuoteOptionalExtraService(QuoteOfferProperties quoteOfferProperties) {
        this.quoteOfferProperties = quoteOfferProperties;
    }

    public List<QuoteOptionalExtra> getAllOptionalExtras() {
        return quoteOfferProperties.getOptionalExtras();
    }

    public List<QuoteOptionalExtra> resolveSelectedExtras(List<String> selectedExtraIds) {
        if (selectedExtraIds == null || selectedExtraIds.isEmpty()) {
            return List.of();
        }

        Set<String> ids = new LinkedHashSet<>(selectedExtraIds);
        return quoteOfferProperties.getOptionalExtras().stream()
                .filter(extra -> extra.getId() != null && ids.contains(extra.getId()))
                .toList();
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
}
