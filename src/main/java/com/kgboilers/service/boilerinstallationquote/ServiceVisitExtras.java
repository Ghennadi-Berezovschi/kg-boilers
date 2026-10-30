package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.QuoteOptionalExtra;
import java.util.List;

/** Server-priced extras offered on service, repair and heating request forms. */
public final class ServiceVisitExtras {
    private ServiceVisitExtras() {}

    public static List<QuoteOptionalExtra> selected(List<String> selections) {
        if (selections == null) return List.of();
        return selections.stream().distinct()
                .filter(title -> "Power flushing".equals(title) || "Magnetic filter".equals(title))
                .map(title -> {
                    QuoteOptionalExtra extra = new QuoteOptionalExtra();
                    extra.setId("Power flushing".equals(title) ? "chemical-power-flush" : "magnetic-filter");
                    extra.setTitle(title);
                    extra.setPriceGbp("Power flushing".equals(title) ? 195 : 145);
                    return extra;
                }).toList();
    }
}
