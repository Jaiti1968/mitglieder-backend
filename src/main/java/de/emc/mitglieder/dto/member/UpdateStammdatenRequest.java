package de.emc.mitglieder.dto.member;

public record UpdateStammdatenRequest(
        String vorname,
        String nachname,
        String ort
) {
}