package de.emc.mitglieder.dto.request;

import de.emc.mitglieder.dto.member.KontaktDto;
import de.emc.mitglieder.dto.member.MitgliedschaftDto;
import de.emc.mitglieder.dto.member.StammdatenDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateMemberRequest {

    private StammdatenDto stammdaten;
    private KontaktDto kontakt;
    private MitgliedschaftDto mitgliedschaft;

}