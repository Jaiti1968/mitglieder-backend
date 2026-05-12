package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberChorkleidungDto {

    private String mitgliedsnummer;

    private String ehemaligeStimme;
    private LocalDate uebergabeAm;
    private String bemerkungUebergabe;

    private boolean neubeschaffung;
    private LocalDate datumAnteil;
    private boolean barzahlung;

    private String bearbeitungsstand;

    private LocalDate rueckgabeAm;
    private String bemerkungRueckgabe;

    private LocalDate kaufdatum;
    private BigDecimal kaufpreis;

    private boolean sommerkleidung;
    private LocalDate sommerkleidungErhalten;
    private LocalDate sommerkleidungRueckgabe;

    // Getter / Setter
}