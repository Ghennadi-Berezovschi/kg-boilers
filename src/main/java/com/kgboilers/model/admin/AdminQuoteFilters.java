package com.kgboilers.model.admin;

import java.time.LocalDate;

public record AdminQuoteFilters(
        String query,
        String serviceType,
        String status,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public boolean hasActiveFilters() {
        return hasText(query)
                || hasText(serviceType)
                || hasText(status)
                || dateFrom != null
                || dateTo != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
