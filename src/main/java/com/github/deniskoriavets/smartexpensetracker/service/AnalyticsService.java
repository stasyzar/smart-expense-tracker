package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.analytics.*;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AnalyticsSummaryDto getSummary(LocalDateTime from, LocalDateTime to) {
        User user = getCurrentUser();

        Long incomes = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(user.getId(),
                TransactionType.INCOME, from, to);
        incomes = incomes == null ? 0 : incomes;

        Long expenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(user.getId(),
                TransactionType.EXPENSE, from, to);
        expenses = expenses == null ? 0 : expenses;

        Long balance = incomes - expenses;

        return new AnalyticsSummaryDto(incomes, expenses, balance);
    }

    public List<CategoryAnalyticsDto> getCategoryAnalytics(LocalDateTime from, LocalDateTime to, TransactionType type) {
        User user = getCurrentUser();

        var projections = transactionRepository.findCategorySumsByUserIdAndDateBetween(
                user.getId(), type, from, to);

        long totalSum = projections.stream().mapToLong(CategorySumProjection::getTotalAmount).sum();

        if (totalSum == 0) {
            return Collections.emptyList();
        }

        return projections.stream().map(categorySumProjection ->
                new CategoryAnalyticsDto(categorySumProjection.getCategoryId(), categorySumProjection.getCategoryName(),
                        categorySumProjection.getTotalAmount(),
                        (double) categorySumProjection.getTotalAmount() / totalSum * 100)).toList();
    }

    public List<MonthlyAnalyticsDto> getMonthlyStats(LocalDateTime from, LocalDateTime to) {
        User user = getCurrentUser();

        var monthlySumProjections = transactionRepository.findMonthlySumsByUserIdAndDateBetween(user.getId(), from, to);

        return monthlySumProjections.stream().map(monthlySumProjection ->
                new MonthlyAnalyticsDto(monthlySumProjection.getPeriod(), monthlySumProjection.getTotalIncome(),
                        monthlySumProjection.getTotalExpense())).toList();
    }

    public List<BudgetStatusDto> getBudgetStatus() {
        User user = getCurrentUser();

        LocalDateTime startOfCurrentMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfCurrentMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59,
                999999999);

        var expenseCategories = categoryRepository.findAllByUserIdAndType(user.getId(), CategoryType.EXPENSE).stream()
                .filter(category -> category.getMonthlyBudget() != null).toList();
        var expenseSums = transactionRepository.findCategorySumsByUserIdAndDateBetween(user.getId(),
                TransactionType.EXPENSE, startOfCurrentMonth, endOfCurrentMonth);

        var categorySumMap = expenseSums.stream()
                .collect(Collectors.toMap(CategorySumProjection::getCategoryId, CategorySumProjection::getTotalAmount));

        return expenseCategories.stream().map(category -> {
                    long totalSpent = categorySumMap.getOrDefault(category.getId(), 0L);
                    boolean isOverBudget = totalSpent > category.getMonthlyBudget();
                    return new BudgetStatusDto(
                            category.getId(),
                            category.getName(),
                            category.getMonthlyBudget(),
                            totalSpent,
                            isOverBudget
                    );
                })
                .toList();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}