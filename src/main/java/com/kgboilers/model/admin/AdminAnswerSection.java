package com.kgboilers.model.admin;

import java.util.List;

public record AdminAnswerSection(String title, List<AdminDetailRow> rows) {
}
