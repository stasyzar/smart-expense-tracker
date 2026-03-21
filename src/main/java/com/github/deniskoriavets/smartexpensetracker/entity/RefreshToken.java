package com.github.deniskoriavets.smartexpensetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RefreshToken implements Serializable {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(unique = true ,nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryDate;

    private boolean revoked;
}
