package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.transaction.CreateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.UpdateTransactionDto;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private Transaction testTransaction;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@email.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashedpassword")
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(testUser);

        jwtToken = jwtService.generateAccessToken(testUser);

        testAccount = Account.builder()
                .user(testUser)
                .name("Test Wallet")
                .currency(Currency.UAH)
                .type(AccountType.CASH)
                .createdAt(LocalDateTime.now())
                .build();
        accountRepository.save(testAccount);

        testCategory = Category.builder()
                .user(testUser)
                .name("Test Category")
                .type(CategoryType.EXPENSE)
                .build();
        categoryRepository.save(testCategory);

        testTransaction = Transaction.builder()
                .account(testAccount)
                .category(testCategory)
                .amount(500L)
                .type(TransactionType.EXPENSE)
                .description("Test")
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(testTransaction);
    }

    @Test
    @DisplayName("Should return 201 Created and the new Transaction")
    void createTransactionShouldReturn201() throws Exception {
        var createRequest = new CreateTransactionDto(
                testAccount.getId(), testCategory.getId(), 1000L,
                TransactionType.EXPENSE, "New Purchase", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(testAccount.getId().toString()))
                .andExpect(jsonPath("$.categoryId").value(testCategory.getId().toString()))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.description").value("New Purchase"));
    }

    @Test
    @DisplayName("Should return 200 OK and list of Transactions by Account ID")
    void getTransactionsByAccountIdShouldReturn200AndList() throws Exception {
        mockMvc.perform(get("/api/transactions/account/{accountId}", testAccount.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].description").value("Test"))
                .andExpect(jsonPath("$[0].amount").value(500))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 200 OK and Transaction by id")
    void getTransactionByIdShouldReturn200AndTransaction() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", testTransaction.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId().toString()))
                .andExpect(jsonPath("$.accountId").value(testAccount.getId().toString()))
                .andExpect(jsonPath("$.categoryId").value(testCategory.getId().toString()))
                .andExpect(jsonPath("$.description").value("Test"))
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 200 OK and updated Transaction")
    void updateTransactionShouldReturn200AndUpdatedTransaction() throws Exception {
        var updateDto = new UpdateTransactionDto(
                testCategory.getId(), 1500L, TransactionType.EXPENSE,
                "Updated Description", LocalDateTime.now()
        );

        mockMvc.perform(put("/api/transactions/{id}", testTransaction.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId().toString()))
                .andExpect(jsonPath("$.accountId").value(testAccount.getId().toString()))
                .andExpect(jsonPath("$.categoryId").value(testCategory.getId().toString()))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.amount").value(1500))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 204 No Content on delete")
    void deleteTransactionShouldReturn204NoContentOnDelete() throws Exception {
        mockMvc.perform(delete("/api/transactions/{id}", testTransaction.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
