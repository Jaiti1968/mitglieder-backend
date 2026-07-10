package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayReportMemberDto {

    private String mitgliedsnummer;

    private String vorname;
    private String nachname;

    private Integer mitgliedsstatusId;
    private String mitgliedsstatus;

    private Integer stimmeId;
    private String stimme;

    private LocalDate geburtsdatum;

    private Integer alterImGeschaeftsjahr;

    private Boolean jubilaeumsgeburtstag;
}
