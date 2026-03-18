package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.account.AccountResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.CreateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.UpdateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.mapper.AccountMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

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
    }

    @Test
    @DisplayName("Should return account by id when user is owner")
    void getAccountByIdShouldReturnAccountWhenUserIsOwner() {
        // Arrange
        mockSecurityContext(testUser.getEmail());

        AccountResponseDto expectedDto = new AccountResponseDto(
                testAccount.getId(), testAccount.getName(), testAccount.getCurrency(),
                testAccount.getType(), testAccount.getCreatedAt(), 0L
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        when(transactionRepository.sumAmountByAccountIdAndType(testAccount.getId(), TransactionType.INCOME))
                .thenReturn(1000L);
        when(transactionRepository.sumAmountByAccountIdAndType(testAccount.getId(), TransactionType.EXPENSE))
                .thenReturn(1000L);

        when(accountMapper.toResponseDto(testAccount, 0L)).thenReturn(expectedDto);

        // Act
        AccountResponseDto actualDto = accountService.getAccountById(testAccount.getId());

        // Assert
        assertNotNull(actualDto);
        assertEquals(expectedDto.id(), actualDto.id());

        verify(accountRepository).findById(testAccount.getId());
        verify(transactionRepository, times(2)).sumAmountByAccountIdAndType(any(), any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not owner")
    void getAccountByIdShouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        User maliciousUser = User.builder()
                .id(UUID.randomUUID())
                .email("hacker@email.com")
                .build();

        mockSecurityContext(maliciousUser.getEmail());

        when(userRepository.findByEmail(maliciousUser.getEmail())).thenReturn(Optional.of(maliciousUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            accountService.getAccountById(testAccount.getId());
        });

        verify(transactionRepository, never()).sumAmountByAccountIdAndType(any(), any());
        verify(accountMapper, never()).toResponseDto(any(), anyLong());
    }

    @Test
    @DisplayName("Should successfully create and return a new account")
    void createAccountShouldReturnCreatedAccount() {
        //Arrange
        mockSecurityContext(testUser.getEmail());
        CreateAccountDto createAccountDto = new CreateAccountDto("Test", Currency.UAH, AccountType.CARD);
        AccountResponseDto expectedDto = new AccountResponseDto(UUID.randomUUID(), "Test", Currency.UAH, AccountType.CARD, LocalDateTime.now(), 0L);
        when(accountMapper.toEntity(any())).thenReturn(testAccount);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountMapper.toResponseDto(testAccount, 0L)).thenReturn(expectedDto);

        //Act
        AccountResponseDto actualDto = accountService.createAccount(createAccountDto);

        //Assert
        assertNotNull(actualDto);
        assertEquals(expectedDto.id(), actualDto.id());
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("Should return all accounts belonging to the current user")
    void getAllAccountsShouldReturnListOfAccounts(){
        //Arrange
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Account testAccount2 = Account.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .user(testUser)
                .currency(Currency.UAH)
                .type(AccountType.CARD)
                .createdAt(LocalDateTime.now())
                .build();
        when(accountRepository.findByUserId(testUser.getId())).thenReturn(List.of(testAccount, testAccount2));
        when(transactionRepository.sumAmountByAccountIdAndType(any(), any())).thenReturn(500L);
        AccountResponseDto expectedDto = new AccountResponseDto(UUID.randomUUID(), "Test", Currency.UAH, AccountType.CARD, LocalDateTime.now(), 0L);
        when(accountMapper.toResponseDto(any(), anyLong())).thenReturn(expectedDto);

        //Act
        List<AccountResponseDto> result = accountService.getAllAccounts();

        //Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionRepository, times(4)).sumAmountByAccountIdAndType(any(), any());
    }

    @Test
    @DisplayName("Should update account details when user is owner")
    void updateAccountShouldReturnUpdatedAccountWhenUserIsOwner(){
        // Arrange
        mockSecurityContext(testUser.getEmail());
        UpdateAccountDto updateDto = new UpdateAccountDto("Updated Name", AccountType.SAVINGS);
        AccountResponseDto expectedDto = new AccountResponseDto(
                testAccount.getId(), "Updated Name", testAccount.getCurrency(),
                AccountType.SAVINGS, testAccount.getCreatedAt(), 0L
        );

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        when(transactionRepository.sumAmountByAccountIdAndType(any(), any())).thenReturn(500L);
        when(accountMapper.toResponseDto(any(), anyLong())).thenReturn(expectedDto);

        // Act
        AccountResponseDto actualDto = accountService.updateAccount(testAccount.getId(), updateDto);

        // Assert
        assertNotNull(actualDto);
        assertEquals("Updated Name", actualDto.name());

        verify(accountMapper).updateAccount(testAccount, updateDto);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException on update when user is not owner")
    void updateAccountShouldThrowExceptionWhenUserIsNotOwner(){
        // Arrange
        User maliciousUser = User.builder().id(UUID.randomUUID()).email("hacker@email.com").build();
        mockSecurityContext(maliciousUser.getEmail());
        UpdateAccountDto updateDto = new UpdateAccountDto("Hacked", AccountType.CASH);

        when(userRepository.findByEmail(maliciousUser.getEmail())).thenReturn(Optional.of(maliciousUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccount(testAccount.getId(), updateDto);
        });

        verify(accountMapper, never()).updateAccount(any(), any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete account when user is owner")
    void deleteAccountShouldReturnDeletedAccountWhenUserIsOwner(){
        //Arrange
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        //Act
        accountService.deleteAccount(testAccount.getId());

        //Assert
        verify(accountRepository).delete(testAccount);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException on delete when user is not owner")
    void deleteAccountShouldThrowExceptionWhenUserIsNotOwner(){
        // Arrange
        User maliciousUser = User.builder().id(UUID.randomUUID()).email("hacker@email.com").build();
        mockSecurityContext(maliciousUser.getEmail());

        when(userRepository.findByEmail(maliciousUser.getEmail())).thenReturn(Optional.of(maliciousUser));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            accountService.deleteAccount(testAccount.getId());
        });

        verify(accountRepository, never()).delete(any());
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);
    }
}