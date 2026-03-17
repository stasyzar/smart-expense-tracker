package com.github.deniskoriavets.smartexpensetracker.entity;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Account {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @ToString.Include
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private AccountType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
}
