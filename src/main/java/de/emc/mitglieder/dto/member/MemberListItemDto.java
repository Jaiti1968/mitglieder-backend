package de.emc.mitglieder.dto.member;

public record MemberListItemDto(
        String mitgliedsnummer,
        String vorname,
        String nachname,
        String ort,
        Integer mitgliedsstatusId,
        String mitgliedsstatus,
        Integer stimmeId,
        String stimme
) {
}