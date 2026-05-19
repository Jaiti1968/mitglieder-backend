package de.emc.mitglieder.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(

        @NotBlank(message = "Rolle ist erforderlich")
        @Pattern(
                regexp = "ADMIN|EDITOR|VIEWER",
                message = "Ungültige Rolle"
        )
        String role
) {
}