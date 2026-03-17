package com.github.deniskoriavets.smartexpensetracker.entity;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
public class User implements UserDetails {
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
