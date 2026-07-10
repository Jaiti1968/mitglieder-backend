package de.emc.mitglieder.reporting.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayReportMemberData {

    private String mitgliedsnummer;

    private String vorname;
    private String nachname;

    private Integer mitgliedsstatusId;
    private String mitgliedsstatus;

    private Integer stimmeId;
    private String stimme;

    private LocalDate geburtsdatum;
}