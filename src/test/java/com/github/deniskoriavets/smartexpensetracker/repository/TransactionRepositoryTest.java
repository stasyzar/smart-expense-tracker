package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should find transactions within date range for specific account")
    void findByAccountIdAndTransactionDateBetweenTest() {
        //Arrange
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());
        var account1 = accountRepository.save(Account.builder()
                .user(user1).name("Wallet 1").type(AccountType.CASH)
                .currency(Currency.UAH).createdAt(LocalDateTime.now()).build());
        var category1 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.EXPENSE)
                .name("Expense")
                .build());
        var transaction1 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.INCOME)
                .amount(333L)
                .build());
        var transaction2 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(7))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.TRANSFER)
                .amount(555L)
                .build());
        var transaction3 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .type(TransactionType.INCOME)
                .amount(123L)
                .build());

        //Act
        var result = transactionRepository.findByAccountIdAndTransactionDateBetween(account1.getId(), LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(1));

        //Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should sum transaction amounts by type for specific account")
    void sumAmountByAccountIdAndTypeTest() {
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());
        var account1 = accountRepository.save(Account.builder()
                .user(user1).name("Wallet 1").type(AccountType.CASH)
                .currency(Currency.UAH).createdAt(LocalDateTime.now()).build());
        var category1 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.EXPENSE)
                .name("Expense")
                .build());
        var transaction1 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(100L)
                .build());
        var transaction2 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(7))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(250L)
                .build());
        var transaction3 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .type(TransactionType.INCOME)
                .amount(500L)
                .build());

        //Act
        var result = transactionRepository.sumAmountByAccountIdAndType(account1.getId(), TransactionType.EXPENSE);

        //Assert
        assertThat(result).isEqualTo(350L);
    }

    @Test
    @DisplayName("Should sum category transactions within date range")
    void sumAmountByCategoryIdAndTransactionDateBetweenTest(){
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());
        var account1 = accountRepository.save(Account.builder()
                .user(user1).name("Wallet 1").type(AccountType.CASH)
                .currency(Currency.UAH).createdAt(LocalDateTime.now()).build());
        var category1 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.EXPENSE)
                .name("Expense")
                .build());
        var transaction1 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(100L)
                .build());
        var transaction2 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(7))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(250L)
                .build());
        var transaction3 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(500L)
                .build());

        //Act
        var result = transactionRepository.sumAmountByCategoryIdAndTransactionDateBetween(category1.getId(), LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(1));

        //Assert
        assertThat(result).isEqualTo(600L);
    }

    @Test
    @DisplayName("Should delete all transactions when account is deleted")
    void cascadeDeleteTest() {
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());
        var account1 = accountRepository.save(Account.builder()
                .user(user1).name("Wallet 1").type(AccountType.CASH)
                .currency(Currency.UAH).createdAt(LocalDateTime.now()).build());
        var category1 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.EXPENSE)
                .name("Expense")
                .build());
        var transaction1 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(100L)
                .build());
        var transaction2 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now().minusDays(7))
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(250L)
                .build());
        var transaction3 = transactionRepository.save(Transaction.builder()
                .account(account1)
                .category(category1)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .type(TransactionType.EXPENSE)
                .amount(500L)
                .build());

        entityManager.flush();
        entityManager.clear();

        //Act
        accountRepository.delete(account1);
        var result = transactionRepository.findAll();

        //Arrange
        assertThat(result).isEmpty();
    }
}
