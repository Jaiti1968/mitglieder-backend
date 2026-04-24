package de.emc.mitglieder.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateStammdatenRequest(
        @Size(max = 50, message = "Anrede darf maximal 50 Zeichen haben")
        String anrede,

        @Size(max = 50, message = "Akademischer Titel darf maximal 50 Zeichen haben")
        String akademischerTitel,

        @NotBlank(message = "Vorname darf nicht leer sein")
        @Size(max = 50, message = "Vorname darf maximal 50 Zeichen haben")
        String vorname,

        @NotBlank(message = "Nachname darf nicht leer sein")
        @Size(max = 50, message = "Nachname darf maximal 50 Zeichen haben")
        String nachname,

        @Size(max = 50, message = "PLZ darf maximal 50 Zeichen haben")
        String plz,

        @Size(max = 50, message = "Ort darf maximal 50 Zeichen haben")
        String ort,

        @Size(max = 50, message = "Straße/Hausnummer darf maximal 50 Zeichen haben")
        String strasseHausNr,

        LocalDate geburtsdatum
) {
}