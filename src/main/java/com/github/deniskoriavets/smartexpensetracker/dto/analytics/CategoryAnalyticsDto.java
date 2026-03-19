package com.github.deniskoriavets.smartexpensetracker.dto.analytics;

import java.util.UUID;

public record CategoryAnalyticsDto(UUID categoryId, String categoryName, Long totalAmount, Double percentage) {
}
