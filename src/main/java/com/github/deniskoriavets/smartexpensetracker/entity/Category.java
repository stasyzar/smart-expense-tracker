package com.github.deniskoriavets.smartexpensetracker.entity;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Category {
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
    private CategoryType type;

    private Long monthlyBudget;

    private String iconCode;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
}
