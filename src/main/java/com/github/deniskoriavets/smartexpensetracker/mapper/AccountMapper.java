package com.github.deniskoriavets.smartexpensetracker.mapper;

import com.github.deniskoriavets.smartexpensetracker.dto.account.AccountResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.CreateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.dto.account.UpdateAccountDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toEntity(CreateAccountDto createAccountDto);

    AccountResponseDto toResponseDto(Account account, long balance);

    void updateAccount(@MappingTarget Account account, UpdateAccountDto updateAccountDto);
}
