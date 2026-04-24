package de.emc.mitglieder.dto.request;

import de.emc.mitglieder.dto.member.KontaktDto;
import de.emc.mitglieder.dto.member.MitgliedschaftDto;
import de.emc.mitglieder.dto.member.StammdatenDto;

public record CreateMemberRequest(
        StammdatenDto stammdaten,
        KontaktDto kontakt,
        MitgliedschaftDto mitgliedschaft
) {
}