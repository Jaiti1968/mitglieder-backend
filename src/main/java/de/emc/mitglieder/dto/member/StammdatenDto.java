package de.emc.mitglieder.dto.member;

import java.time.LocalDate;

public record StammdatenDto(
        String anrede,
        String akademischerTitel,
        String vorname,
        String nachname,
        String plz,
        String ort,
        String strasseHausNr,
        LocalDate geburtsdatum
) {
}