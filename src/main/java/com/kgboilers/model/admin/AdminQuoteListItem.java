package com.kgboilers.model.admin;

public record AdminQuoteListItem(
        Long id,
        String createdAt,
        String status,
        String serviceType,
        String clientName,
        String clientEmail,
        String clientPhone,
        String postcode,
        String selectedWork,
        Integer totalPriceGbp
) {
}
