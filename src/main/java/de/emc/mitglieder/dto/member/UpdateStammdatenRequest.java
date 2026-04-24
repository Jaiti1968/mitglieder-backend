package de.emc.mitglieder.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStammdatenRequest(
        @NotBlank(message = "Vorname darf nicht leer sein")
        @Size(max = 50, message = "Vorname darf maximal 50 Zeichen haben")
        String vorname,

        @NotBlank(message = "Nachname darf nicht leer sein")
        @Size(max = 50, message = "Nachname darf maximal 50 Zeichen haben")
        String nachname,

        @Size(max = 50, message = "Ort darf maximal 50 Zeichen haben")
        String ort
) {
}