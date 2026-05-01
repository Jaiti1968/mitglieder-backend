package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailDto {

        private String mitgliedsnummer;
        private StammdatenDto stammdaten;
        private KontaktDto kontakt;
        private MitgliedschaftDto mitgliedschaft;

}