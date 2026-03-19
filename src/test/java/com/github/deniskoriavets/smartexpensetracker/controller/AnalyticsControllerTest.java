package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.Transaction;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.*;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import com.github.deniskoriavets.smartexpensetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        now = LocalDateTime.now();

        User testUser = User.builder()
                .email("test@email.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashedpassword")
                .role(Role.USER)
                .isActive(true)
                .createdAt(now)
                .build();
        userRepository.save(testUser);

        jwtToken = jwtService.generateAccessToken(testUser);

        Account testAccount = Account.builder()
                .user(testUser)
                .name("Test Wallet")
                .currency(Currency.UAH)
                .type(AccountType.CASH)
                .createdAt(now)
                .build();
        accountRepository.save(testAccount);

        Category expenseCategory = Category.builder()
                .user(testUser)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .monthlyBudget(1000L)
                .build();
        categoryRepository.save(expenseCategory);

        Category incomeCategory = Category.builder()
                .user(testUser)
                .name("Salary")
                .type(CategoryType.INCOME)
                .build();
        categoryRepository.save(incomeCategory);

        transactionRepository.save(Transaction.builder()
                .account(testAccount)
                .category(expenseCategory)
                .amount(500L)
                .type(TransactionType.EXPENSE)
                .transactionDate(now)
                .createdAt(now)
                .build());

        transactionRepository.save(Transaction.builder()
                .account(testAccount)
                .category(incomeCategory)
                .amount(1500L)
                .type(TransactionType.INCOME)
                .transactionDate(now)
                .createdAt(now)
                .build());
    }

    @Test
    @DisplayName("Should return 200 OK and summary analytics")
    void getSummaryShouldReturn200AndSummary() throws Exception {
        String from = now.minusDays(1).toString();
        String to = now.plusDays(1).toString();

        mockMvc.perform(get("/api/analytics/summary")
                        .param("from", from)
                        .param("to", to)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1500))
                .andExpect(jsonPath("$.totalExpense").value(500))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @DisplayName("Should return 200 OK and categories analytics")
    void getCategoriesAnalyticsShouldReturn200AndList() throws Exception {
        String from = now.minusDays(1).toString();
        String to = now.plusDays(1).toString();

        mockMvc.perform(get("/api/analytics/categories")
                        .param("from", from)
                        .param("to", to)
                        .param("transactionType", "EXPENSE")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].totalAmount").value(500))
                .andExpect(jsonPath("$[0].percentage").value(100.0));
    }

    @Test
    @DisplayName("Should return 200 OK and monthly stats")
    void getMonthlyStatsShouldReturn200AndList() throws Exception {
        String from = now.minusDays(1).toString();
        String to = now.plusDays(1).toString();

        String expectedPeriod = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        mockMvc.perform(get("/api/analytics/monthly")
                        .param("from", from)
                        .param("to", to)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].period").value(expectedPeriod))
                .andExpect(jsonPath("$[0].totalIncome").value(1500))
                .andExpect(jsonPath("$[0].totalExpense").value(500));
    }

    @Test
    @DisplayName("Should return 200 OK and budget statuses")
    void getBudgetStatusesShouldReturn200AndList() throws Exception {
        // Тут параметри from та to не передаємо, бо ми їх прибрали з контролера
        mockMvc.perform(get("/api/analytics/budgets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].monthlyBudget").value(1000))
                .andExpect(jsonPath("$[0].totalSpent").value(500))
                .andExpect(jsonPath("$[0].isOverBudget").value(false));
    }
}