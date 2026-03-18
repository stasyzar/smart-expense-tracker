package com.github.deniskoriavets.smartexpensetracker.mapper;

import com.github.deniskoriavets.smartexpensetracker.dto.transaction.CreateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.TransactionResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.transaction.UpdateTransactionDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "category", ignore = true)
    Transaction toEntity(CreateTransactionDto createTransactionDto);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "category.id", target = "categoryId")
    TransactionResponseDto toDto(Transaction transaction);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateTransaction(@MappingTarget Transaction transaction, UpdateTransactionDto updateTransactionDto);
}