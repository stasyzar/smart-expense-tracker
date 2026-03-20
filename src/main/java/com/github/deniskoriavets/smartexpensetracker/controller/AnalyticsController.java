package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.analytics.*;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Статистика")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Загальна статистика", description = "Повертає суму доходів, витрат та загальний баланс за обраний період дат")
    public ResponseEntity<AnalyticsSummaryDto> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        return ResponseEntity.ok(analyticsService.getSummary(from, to));
    }

    @GetMapping("/categories")
    @Operation(summary = "Статистика за категоріями", description = "Повертає суму та відсоток по кожній категорії (доходи або витрати) за період")
    public ResponseEntity<List<CategoryAnalyticsDto>> getCategoriesAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam TransactionType transactionType) {
        return ResponseEntity.ok(analyticsService.getCategoryAnalytics(from, to, transactionType));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Місячна статистика", description = "Повертає агреговані доходи та витрати згруповані по місяцях")
    public ResponseEntity<List<MonthlyAnalyticsDto>> getMonthlyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(analyticsService.getMonthlyStats(from, to));
    }

    @GetMapping("/budgets")
    @Operation(summary = "Статус бюджетів", description = "Повертає інформацію про ліміти категорій та поточні витрати користувача у поточному місяці")
    public ResponseEntity<List<BudgetStatusDto>> getBudgetStatuses() {
        return ResponseEntity.ok(analyticsService.getBudgetStatus());
    }
}