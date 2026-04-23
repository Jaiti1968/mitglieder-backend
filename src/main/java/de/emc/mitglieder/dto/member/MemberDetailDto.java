package de.emc.mitglieder.dto.member;

public record MemberDetailDto(
        String mitgliedsnummer,
        StammdatenDto stammdaten,
        KontaktDto kontakt,
        MitgliedschaftDto mitgliedschaft
) {
}