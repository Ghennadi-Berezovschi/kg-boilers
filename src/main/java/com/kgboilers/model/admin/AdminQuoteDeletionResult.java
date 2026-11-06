package com.kgboilers.model.admin;

import java.util.List;

public record AdminQuoteDeletionResult(int deletedCount, List<String> undeletedPictures) {
}
