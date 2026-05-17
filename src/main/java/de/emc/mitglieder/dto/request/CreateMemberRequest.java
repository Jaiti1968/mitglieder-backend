package de.emc.mitglieder.dto.request;

import de.emc.mitglieder.dto.member.KontaktDto;
import de.emc.mitglieder.dto.member.MitgliedschaftDto;
import de.emc.mitglieder.dto.member.StammdatenDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateMemberRequest {

    @NotNull(message = "Stammdaten müssen angegeben werden")
    @Valid
    private StammdatenDto stammdaten;

    @Valid
    private KontaktDto kontakt;

    @NotNull(message = "Mitgliedschaft muss angegeben werden")
    @Valid
    private MitgliedschaftDto mitgliedschaft;

}