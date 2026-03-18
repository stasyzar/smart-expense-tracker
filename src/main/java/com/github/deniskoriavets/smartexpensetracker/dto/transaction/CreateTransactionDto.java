package com.github.deniskoriavets.smartexpensetracker.dto.transaction;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransactionDto(
        @NotNull UUID accountId,
        @NotNull UUID categoryId,
        @NotNull @Positive Long amount,
        @NotNull TransactionType type,
        String description,
        @NotNull LocalDateTime transactionDate
) {}