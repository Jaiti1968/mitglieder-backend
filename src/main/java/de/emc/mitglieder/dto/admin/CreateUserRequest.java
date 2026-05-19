package de.emc.mitglieder.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Benutzername ist erforderlich")
        @Size(max = 50, message = "Benutzername maximal 50 Zeichen")
        String username,

        @NotBlank(message = "Passwort ist erforderlich")
        @Size(min = 8, message = "Passwort mindestens 8 Zeichen")
        String password,

        @NotBlank(message = "Rolle ist erforderlich")
        @Pattern(
                regexp = "ADMIN|EDITOR|VIEWER",
                message = "Ungültige Rolle"
        )
        String role
) {
}