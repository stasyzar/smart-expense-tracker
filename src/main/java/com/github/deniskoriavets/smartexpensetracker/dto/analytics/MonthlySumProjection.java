package com.github.deniskoriavets.smartexpensetracker.dto.analytics;

public interface MonthlySumProjection {
    String getPeriod();
    Long getTotalIncome();
    Long getTotalExpense();
}