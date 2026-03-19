package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.analytics.*;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@email.com")
                .build();
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should calculate summary correctly")
    void getSummaryShouldReturnCorrectCalculations() {
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(testUser.getId(), TransactionType.INCOME, from, to))
                .thenReturn(10000L);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(testUser.getId(), TransactionType.EXPENSE, from, to))
                .thenReturn(4000L);

        AnalyticsSummaryDto result = analyticsService.getSummary(from, to);

        assertNotNull(result);
        assertEquals(10000L, result.totalIncome());
        assertEquals(4000L, result.totalExpense());
        assertEquals(6000L, result.balance());
    }

    @Test
    @DisplayName("Should return empty list for category analytics when no transactions")
    void getCategoryAnalyticsShouldReturnEmptyWhenNoTransactions() {
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        when(transactionRepository.findCategorySumsByUserIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        var result = analyticsService.getCategoryAnalytics(LocalDateTime.now(), LocalDateTime.now(), TransactionType.EXPENSE);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should calculate category percentages correctly")
    void getCategoryAnalyticsShouldCalculatePercentages() {
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        CategorySumProjection proj1 = mock(CategorySumProjection.class);
        when(proj1.getCategoryId()).thenReturn(UUID.randomUUID());
        when(proj1.getCategoryName()).thenReturn("Food");
        when(proj1.getTotalAmount()).thenReturn(600L);

        CategorySumProjection proj2 = mock(CategorySumProjection.class);
        when(proj2.getCategoryId()).thenReturn(UUID.randomUUID());
        when(proj2.getCategoryName()).thenReturn("Transport");
        when(proj2.getTotalAmount()).thenReturn(400L);

        when(transactionRepository.findCategorySumsByUserIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of(proj1, proj2));

        var result = analyticsService.getCategoryAnalytics(LocalDateTime.now(), LocalDateTime.now(), TransactionType.EXPENSE);

        assertEquals(2, result.size());
        assertEquals(60.0, result.get(0).percentage());
        assertEquals(40.0, result.get(1).percentage());
    }

    @Test
    @DisplayName("Should return monthly stats mapped correctly")
    void getMonthlyStatsShouldReturnMappedData() {
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        MonthlySumProjection proj = mock(MonthlySumProjection.class);
        when(proj.getPeriod()).thenReturn("2023-10");
        when(proj.getTotalIncome()).thenReturn(5000L);
        when(proj.getTotalExpense()).thenReturn(2000L);

        when(transactionRepository.findMonthlySumsByUserIdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of(proj));

        var result = analyticsService.getMonthlyStats(LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1, result.size());
        assertEquals("2023-10", result.getFirst().period());
        assertEquals(5000L, result.getFirst().totalIncome());
    }

    @Test
    @DisplayName("Should calculate budget status and correctly identify over-budget categories")
    void getBudgetStatusShouldIdentifyOverBudget() {
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .monthlyBudget(1000L)
                .build();

        when(categoryRepository.findAllByUserIdAndType(testUser.getId(), CategoryType.EXPENSE))
                .thenReturn(List.of(category));

        CategorySumProjection proj = mock(CategorySumProjection.class);
        when(proj.getCategoryId()).thenReturn(categoryId);
        when(proj.getTotalAmount()).thenReturn(1500L);

        when(transactionRepository.findCategorySumsByUserIdAndDateBetween(eq(testUser.getId()), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(List.of(proj));

        var result = analyticsService.getBudgetStatus();

        assertEquals(1, result.size());
        assertEquals(1500L, result.getFirst().totalSpent());
        assertTrue(result.getFirst().isOverBudget());
    }
}