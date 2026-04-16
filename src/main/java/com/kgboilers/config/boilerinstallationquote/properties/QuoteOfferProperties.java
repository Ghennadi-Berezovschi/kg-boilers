package com.kgboilers.config.boilerinstallationquote.properties;

import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "kg.quote-offer")
public class QuoteOfferProperties {

    private List<String> includedItems = new ArrayList<>();
    private List<QuoteOptionalExtra> optionalExtras = new ArrayList<>();

    public List<String> getIncludedItems() {
        return includedItems;
    }

    public void setIncludedItems(List<String> includedItems) {
        this.includedItems = includedItems;
    }

    public List<QuoteOptionalExtra> getOptionalExtras() {
        return optionalExtras;
    }

    public void setOptionalExtras(List<QuoteOptionalExtra> optionalExtras) {
        this.optionalExtras = optionalExtras;
    }
}
