package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.analytics.*;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        return ResponseEntity.ok(analyticsService.getSummary(from, to));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryAnalyticsDto>> getCategoriesAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam TransactionType transactionType) {
        return ResponseEntity.ok(analyticsService.getCategoryAnalytics(from, to, transactionType));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyAnalyticsDto>> getMonthlyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(analyticsService.getMonthlyStats(from, to));
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetStatusDto>> getBudgetStatuses() {
        return ResponseEntity.ok(analyticsService.getBudgetStatus());
    }
}