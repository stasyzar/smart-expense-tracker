package com.github.deniskoriavets.smartexpensetracker.dto.transaction;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDto(
        UUID id,
        UUID accountId,
        UUID categoryId,
        Long amount,
        TransactionType type,
        String description,
        LocalDateTime transactionDate,
        LocalDateTime createdAt
) {}