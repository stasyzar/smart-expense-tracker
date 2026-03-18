package com.github.deniskoriavets.smartexpensetracker.dto.account;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponseDto(UUID id, String name, Currency currency, AccountType type, LocalDateTime createdAt, long balance) {
}
