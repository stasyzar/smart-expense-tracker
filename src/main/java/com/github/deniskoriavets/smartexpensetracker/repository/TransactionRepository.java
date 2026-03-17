package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.Transaction;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("select t from Transaction t where t.account.id = :accountId and t.transactionDate between :startDate and :endDate")
    List<Transaction> findByAccountIdAndTransactionDateBetween(UUID accountId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("select sum(t.amount) from Transaction t where t.account.id = :accountId and t.type = :type")
    Long sumAmountByAccountIdAndType(UUID accountId, TransactionType type);

    @Query("select sum(t.amount) from Transaction t where t.category.id = :categoryId and t.transactionDate between :startDate and :endDate")
    Long sumAmountByCategoryIdAndTransactionDateBetween(UUID categoryId, LocalDateTime startDate, LocalDateTime endDate);
}
