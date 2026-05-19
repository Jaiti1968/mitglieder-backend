package de.emc.mitglieder.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordRequest(

        @NotBlank(message = "Passwort ist erforderlich")
        @Size(min = 8, message = "Passwort mindestens 8 Zeichen")
        String password
) {
}