package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.dto.analytics.CategorySumProjection;
import com.github.deniskoriavets.smartexpensetracker.dto.analytics.MonthlySumProjection;
import com.github.deniskoriavets.smartexpensetracker.entity.Transaction;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (cast(:fromDate as timestamp) IS NULL OR t.transactionDate >= :fromDate) " +
            "AND (cast(:toDate as timestamp) IS NULL OR t.transactionDate <= :toDate)")
    Page<Transaction> findByAccountIdAndFilters(
            UUID accountId,
            TransactionType type,
             UUID categoryId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.type = :type " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    Long sumAmountByUserIdAndTypeAndDateBetween(
            UUID userId,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT t.category.id as categoryId, t.category.name as categoryName, SUM(t.amount) as totalAmount " +
            "FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.type = :type " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
            "GROUP BY t.category.id, t.category.name")
    List<CategorySumProjection> findCategorySumsByUserIdAndDateBetween(
            UUID userId,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT FUNCTION('to_char', t.transactionDate, 'YYYY-MM') as period, " +
            "COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0L END), 0L) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0L END), 0L) as totalExpense " +
            "FROM Transaction t " +
            "WHERE t.account.user.id = :userId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
            "GROUP BY FUNCTION('to_char', t.transactionDate, 'YYYY-MM') " +
            "ORDER BY FUNCTION('to_char', t.transactionDate, 'YYYY-MM')")
    List<MonthlySumProjection> findMonthlySumsByUserIdAndDateBetween(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
