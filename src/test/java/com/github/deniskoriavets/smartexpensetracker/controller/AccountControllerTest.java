package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.account.CreateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.UpdateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private Account testAccount;
    private String jwtToken;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("Should return 200 OK and list of user accounts")
    void getAllAccountsShouldReturn200AndList() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Wallet"))
                .andExpect(jsonPath("$[0].currency").value("UAH"));
    }

    @Test
    @DisplayName("Should return 201 Created and the new account")
    void createAccountShouldReturn201() throws Exception {
        var createRequest = new CreateAccountDto("New Wallet", Currency.USD, AccountType.CARD);

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Wallet"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("Should return 200 OK and account by id")
    void getAccountByIdShouldReturn200AndAccountById() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", testAccount.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Wallet"))
                .andExpect(jsonPath("$.currency").value("UAH"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("Should return 200 OK and updated account")
    void updateAccountShouldReturn200AndAccountUpdated() throws Exception {
        var updateDto = new UpdateAccountDto("Updated Wallet", AccountType.CARD);

        mockMvc.perform(put("/api/accounts/{id}", testAccount.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Wallet"))
                .andExpect(jsonPath("$.currency").value("UAH"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("Should return 204 No Content on delete")
    void deleteAccountShouldReturn204NoContentOnDelete() throws Exception {
        mockMvc.perform(delete("/api/accounts/{id}", testAccount.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}