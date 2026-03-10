package com.github.deniskoriavets.smartexpensetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(unique=true, nullable=false)
    @ToString.Include
    private String email;

    @Column(nullable=false)
    private String passwordHash;

    @Column(nullable=false)
    @ToString.Include
    private String firstName;

    @Column(nullable=false)
    @ToString.Include
    private String lastName;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Role role;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false)
    private boolean isActive;
}
