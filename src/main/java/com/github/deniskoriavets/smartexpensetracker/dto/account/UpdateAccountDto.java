package com.github.deniskoriavets.smartexpensetracker.dto.account;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;

public record UpdateAccountDto(String name, AccountType type) {
}
