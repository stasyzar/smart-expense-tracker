package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.transaction.CreateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.TransactionResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.UpdateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.mapper.TransactionMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.AccountRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.TransactionRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public TransactionResponseDto createTransaction(CreateTransactionDto dto) {
        var user = getCurrentUser();

        var account = accountRepository.findById(dto.accountId()).orElseThrow(EntityNotFoundException::new);
        if (!account.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();
        var category = categoryRepository.findById(dto.categoryId()).orElseThrow(EntityNotFoundException::new);
        if (!category.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();

        var transaction = transactionMapper.toEntity(dto);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setCreatedAt(LocalDateTime.now());
        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    public Page<TransactionResponseDto> getTransactionsByAccountId(
            UUID accountId,
            TransactionType type,
            UUID categoryId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {

        var user = getCurrentUser();
        var account = accountRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        if (!account.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();

        return transactionRepository.findByAccountIdAndFilters(accountId, type, categoryId, from, to, pageable)
                .map(transactionMapper::toDto);
    }

    public TransactionResponseDto getTransactionById(UUID id) {
        var user = getCurrentUser();

        var transaction = transactionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if(!transaction.getAccount().getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();

        return transactionMapper.toDto(transaction);
    }

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public TransactionResponseDto updateTransaction(UUID id, UpdateTransactionDto dto) {
        var user = getCurrentUser();

        var transaction = transactionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if(!transaction.getAccount().getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();

        var category = categoryRepository.findById(dto.categoryId()).orElseThrow(EntityNotFoundException::new);
        if(!category.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();

        transactionMapper.updateTransaction(transaction, dto);
        transaction.setCategory(category);

        transactionRepository.save(transaction);
        return transactionMapper.toDto(transaction);
    }

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public void deleteTransaction(UUID id) {
        var user = getCurrentUser();

        var transaction = transactionRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if(!transaction.getAccount().getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();
        transactionRepository.delete(transaction);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}