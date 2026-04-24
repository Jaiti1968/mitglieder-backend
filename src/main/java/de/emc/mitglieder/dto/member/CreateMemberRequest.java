package de.emc.mitglieder.dto.member;

public record CreateMemberRequest(
        StammdatenDto stammdaten,
        KontaktDto kontakt,
        MitgliedschaftDto mitgliedschaft
) {
}