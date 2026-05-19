package de.emc.mitglieder.dto.admin;

import java.time.LocalDateTime;

public record UserAdminResponse(
        Long id,
        String username,
        String role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}