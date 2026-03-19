package com.github.deniskoriavets.smartexpensetracker.dto.analytics;

import java.util.UUID;

public interface CategorySumProjection {
    UUID getCategoryId();
    String getCategoryName();
    Long getTotalAmount();
}