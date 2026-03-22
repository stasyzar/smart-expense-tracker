package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.account.AccountResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.CreateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.UpdateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Управління рахунками")
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Отримати всі рахунки", description = "Повертає список всіх рахунків поточного авторизованого користувача")
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping
    @Operation(summary = "Створити рахунок", description = "Створює новий рахунок (гаманець, картка тощо) для поточного користувача")
    public ResponseEntity<AccountResponseDto> createAccount(@RequestBody @Valid CreateAccountDto createAccountDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(createAccountDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати рахунок за ID", description = "Повертає детальну інформацію про конкретний рахунок")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити рахунок", description = "Оновлює назву або тип існуючого рахунку")
    public ResponseEntity<AccountResponseDto> updateAccount(@PathVariable UUID id, @RequestBody @Valid UpdateAccountDto updateAccountDto) {
        return ResponseEntity.ok(accountService.updateAccount(id, updateAccountDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити рахунок", description = "Видаляє рахунок та всі пов'язані з ним транзакції")    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
