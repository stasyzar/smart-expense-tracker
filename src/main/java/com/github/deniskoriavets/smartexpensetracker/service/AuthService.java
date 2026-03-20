package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.auth.AuthenticationResponse;
import com.github.deniskoriavets.smartexpensetracker.dto.auth.LoginRequest;
import com.github.deniskoriavets.smartexpensetracker.dto.auth.RegisterRequest;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.RefreshToken;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import com.github.deniskoriavets.smartexpensetracker.exception.TokenException;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.RefreshTokenRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import com.github.deniskoriavets.smartexpensetracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        userRepository.save(user);

        createDefaultCategoriesForUser(user);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        log.info("New user successfully registered with email: {}", request.email());

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserRefreshToken(user, refreshToken);

        log.info("User {} successfully authenticated", request.email());

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        var tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new TokenException("Refresh token is expired or revoked");
        }

        var user = tokenEntity.getUser();
        var newAccessToken = jwtService.generateAccessToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.delete(tokenEntity);
        saveUserRefreshToken(user, newRefreshToken);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private void saveUserRefreshToken(User user, String refreshToken) {
        var refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private void createDefaultCategoriesForUser(User user) {
        List<Category> defaultCategories = List.of(
                Category.builder().user(user).name("Продукти").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("Транспорт").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("Житло та комуналка").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("Розваги").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("Здоров'я").type(CategoryType.EXPENSE).build(),
                Category.builder().user(user).name("Зарплата").type(CategoryType.INCOME).build(),
                Category.builder().user(user).name("Подарунки").type(CategoryType.INCOME).build()
        );
        categoryRepository.saveAll(defaultCategories);
    }
}
