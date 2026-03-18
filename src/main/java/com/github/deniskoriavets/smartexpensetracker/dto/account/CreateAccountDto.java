package com.github.deniskoriavets.smartexpensetracker.dto.account;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountDto(@NotBlank String name, @NotNull Currency currency, @NotNull AccountType type) {
}
