package com.github.deniskoriavets.smartexpensetracker.dto.user;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id, 
        String email, 
        String firstName, 
        String lastName, 
        Role role, 
        boolean isActive, 
        LocalDateTime createdAt
) {}