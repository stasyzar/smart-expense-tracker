package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.account.AccountResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.CreateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.UpdateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.mapper.AccountMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;

    public AccountResponseDto createAccount(CreateAccountDto createAccountDto) {
        User user = getCurrentUser();
        Account account = accountMapper.toEntity(createAccountDto);
        account.setUser(user);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return accountMapper.toResponseDto(account, 0L);
    }

    public List<AccountResponseDto> getAllAccounts() {
        User user = getCurrentUser();
        return accountRepository.findByUserId(user.getId()).stream().map(account -> accountMapper.toResponseDto(account, calculateBalance(account.getId()))).toList();
    }

    public AccountResponseDto getAccountById(UUID id) {
        User user = getCurrentUser();
        Account account = accountRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new EntityNotFoundException();
        }
        return accountMapper.toResponseDto(account, calculateBalance(account.getId()));
    }

    public AccountResponseDto updateAccount(UUID id, UpdateAccountDto updateAccountDto) {
        User user = getCurrentUser();
        Account account = accountRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new EntityNotFoundException();
        }
        accountMapper.updateAccount(account, updateAccountDto);
        return accountMapper.toResponseDto(account, calculateBalance(account.getId()));
    }

    public void deleteAccount(UUID id) {
        User user = getCurrentUser();
        Account account = accountRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new EntityNotFoundException();
        }
        accountRepository.delete(account);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private long calculateBalance(UUID accountId) {
        Long incomes = transactionRepository.sumAmountByAccountIdAndType(accountId, TransactionType.INCOME);
        if (incomes == null)
            incomes = 0L;
        Long expenses = transactionRepository.sumAmountByAccountIdAndType(accountId, TransactionType.EXPENSE);
        if (expenses == null)
            expenses = 0L;
        return incomes - expenses;
    }
}
