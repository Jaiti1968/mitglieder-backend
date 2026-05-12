package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberListItemDto {

    private String mitgliedsnummer;
    private Boolean personFirma;
    private String vorname;
    private String nachname;
    private String ort;
    private Integer mitgliedsstatusId;
    private String mitgliedsstatus;
    private Integer stimmeId;
    private String stimme;

}