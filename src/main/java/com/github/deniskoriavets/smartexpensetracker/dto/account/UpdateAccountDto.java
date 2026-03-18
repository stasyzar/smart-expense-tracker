package com.github.deniskoriavets.smartexpensetracker.dto.account;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountDto(@NotBlank String name, @NotNull AccountType type) {
}
