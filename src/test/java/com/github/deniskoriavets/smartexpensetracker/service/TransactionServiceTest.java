package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.transaction.CreateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.TransactionResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.UpdateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.Transaction;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.*;
import com.github.deniskoriavets.smartexpensetracker.mapper.TransactionMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@email.com")
                .firstName("Test")
                .lastName("Test")
                .role(Role.USER)
                .passwordHash("password")
                .createdAt(LocalDateTime.now())
                .build();
        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Test")
                .currency(Currency.UAH)
                .type(AccountType.CARD)
                .createdAt(LocalDateTime.now())
                .build();
        testCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Test")
                .type(CategoryType.EXPENSE)
                .build();
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(555L)
                .build();
    }

    @Test
    @DisplayName("Should successfully create and return a new transaction")
    void createTransactionShouldReturnCreatedTransaction() {
        // Arrange
        mockSecurityContext(testUser.getEmail());
        var createDto = new CreateTransactionDto(
                testAccount.getId(), testCategory.getId(), 500L,
                TransactionType.EXPENSE, "Test", LocalDateTime.now()
        );
        var expectedDto = new TransactionResponseDto(
                testTransaction.getId(), testAccount.getId(), testCategory.getId(),
                500L, TransactionType.EXPENSE, "Test",
                testTransaction.getTransactionDate(), testTransaction.getCreatedAt()
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        when(transactionMapper.toEntity(any())).thenReturn(testTransaction);
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        when(transactionMapper.toDto(any())).thenReturn(expectedDto);

        // Act
        var actualDto = transactionService.createTransaction(createDto);

        // Assert
        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should return paginated list of transactions for a specific account")
    void getTransactionsByAccountShouldReturnPaginatedListOfTransactions() {
        // Arrange
        mockSecurityContext(testUser.getEmail());
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(testTransaction));

        var expectedDto = new TransactionResponseDto(
                testTransaction.getId(), testAccount.getId(), testCategory.getId(),
                500L, TransactionType.EXPENSE, "Test",
                testTransaction.getTransactionDate(), testTransaction.getCreatedAt()
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        when(transactionRepository.findByAccountIdAndFilters(eq(testAccount.getId()), any(), any(), any(), any(), eq(pageable))).thenReturn(transactionPage);
        when(transactionMapper.toDto(any())).thenReturn(expectedDto);

        // Act
        Page<TransactionResponseDto> result = transactionService.getTransactionsByAccountId(testAccount.getId(), null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedDto, result.getContent().getFirst());
    }

    @Test
    @DisplayName("Should return transaction by id when user is owner")
    void getTransactionByIdShouldReturnTransactionWhenUserIsOwner() {
        // Arrange
        mockSecurityContext(testUser.getEmail());

        var expectedDto = new TransactionResponseDto(
                testTransaction.getId(), testAccount.getId(), testCategory.getId(),
                500L, TransactionType.EXPENSE, "Test",
                testTransaction.getTransactionDate(), testTransaction.getCreatedAt()
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(transactionMapper.toDto(any())).thenReturn(expectedDto);

        // Act
        var result = transactionService.getTransactionById(testTransaction.getId());

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(transactionRepository).findById(testTransaction.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getting transaction and user is not owner")
    void getTransactionByIdShouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        User maliciousUser = User.builder().id(UUID.randomUUID()).email("hacker@email.com").build();
        mockSecurityContext(maliciousUser.getEmail());

        when(userRepository.findByEmail(maliciousUser.getEmail())).thenReturn(Optional.of(maliciousUser));
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> transactionService.getTransactionById(testTransaction.getId()));
        verify(transactionMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should update and return transaction when user is owner")
    void updateTransactionShouldReturnUpdatedTransactionWhenUserIsOwner() {
        // Arrange
        mockSecurityContext(testUser.getEmail());

        var updateDto = new UpdateTransactionDto(
                testCategory.getId(), 1000L, TransactionType.EXPENSE, "Updated Name", LocalDateTime.now()
        );

        var expectedDto = new TransactionResponseDto(
                testTransaction.getId(), testAccount.getId(), testCategory.getId(),
                1000L, TransactionType.EXPENSE, "Updated Name",
                updateDto.transactionDate(), testTransaction.getCreatedAt()
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(transactionMapper.toDto(any())).thenReturn(expectedDto);
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        // Act
        var result = transactionService.updateTransaction(testTransaction.getId(), updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(transactionMapper).updateTransaction(any(), any());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should delete transaction when user is owner")
    void deleteTransactionShouldRemoveTransactionWhenUserIsOwner() {
        // Arrange
        mockSecurityContext(testUser.getEmail());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));

        // Act
        transactionService.deleteTransaction(testTransaction.getId());

        // Assert
        verify(transactionRepository).delete(testTransaction);
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);
    }
}
