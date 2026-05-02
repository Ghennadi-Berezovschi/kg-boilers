package com.kgboilers.model.admin;

import java.util.List;

public record AdminQuoteDetail(
        Long id,
        String createdAt,
        String updatedAt,
        String contactRequestedAt,
        String status,
        String serviceType,
        String clientName,
        String clientEmail,
        String clientPhone,
        String postcode,
        String selectedWork,
        Integer totalPriceGbp,
        List<AdminQuoteExtra> extras,
        List<AdminUploadedPicture> uploadedPictures,
        List<AdminUploadedPicture> jobPictures,
        List<AdminDetailRow> answers
) {
}
