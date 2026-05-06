package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.transaction.CreateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.TransactionResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.UpdateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import com.github.deniskoriavets.smartexpensetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Управління транзакціями")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Створити транзакцію", description = "Додає нову транзакцію до вказаного рахунку та категорії")
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody @Valid CreateTransactionDto createTransactionDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(createTransactionDto));
    }

    @GetMapping
    @Operation(summary = "Отримати всі транзакції", description = "Повертає список всіх транзакцій поточного користувача для дашборду")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Отримати транзакції рахунку", description = "Повертає пагінований список транзакцій для конкретного рахунку")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByAccountId(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(
                accountId, type, categoryId, from, to, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати транзакцію за ID", description = "Повертає деталі конкретної транзакції")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити транзакцію", description = "Змінює суму, опис, категорію або дату існуючої транзакції")
    public ResponseEntity<TransactionResponseDto> updateTransaction(@PathVariable UUID id, @RequestBody @Valid UpdateTransactionDto updateTransactionDto) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.updateTransaction(id, updateTransactionDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити транзакцію", description = "Видаляє транзакцію")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}