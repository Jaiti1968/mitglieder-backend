package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembersDirectoryReportMemberDto {

    private String mitgliedsnummer;

    private String vorname;
    private String nachname;

    private String plz;
    private String ort;
    private String strasseHausNr;

    private LocalDate geburtsdatum;

    private String telefonPrivat;
    private String telefonGeschaeftlich;
    private String mobiltelefon;
    private String email;

    private Integer mitgliedsstatusId;
    private String mitgliedsstatus;
    private Integer stimmeId;
    private String stimme;

    private Boolean kammerchor;
}
