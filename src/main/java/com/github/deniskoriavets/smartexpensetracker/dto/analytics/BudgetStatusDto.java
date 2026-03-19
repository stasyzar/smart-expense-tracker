package com.github.deniskoriavets.smartexpensetracker.dto.analytics;

import java.util.UUID;

public record BudgetStatusDto(UUID categoryId, String categoryName, Long monthlyBudget, Long totalSpent, Boolean isOverBudget) {
}
