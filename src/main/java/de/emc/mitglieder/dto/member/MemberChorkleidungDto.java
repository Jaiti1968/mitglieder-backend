package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberChorkleidungDto {

    private String mitgliedsnummer;

    private String ehemaligeStimme;
    private LocalDateTime uebergabeAm;
    private String bemerkungUebergabe;

    private boolean neubeschaffung;
    private LocalDateTime datumAnteil;
    private boolean barzahlung;

    private String bearbeitungsstand;

    private LocalDateTime rueckgabeAm;
    private String bemerkungRueckgabe;

    private LocalDateTime kaufdatum;
    private BigDecimal kaufpreis;

    private boolean sommerkleidung;
    private LocalDateTime sommerkleidungErhalten;
    private LocalDateTime sommerkleidungRueckgabe;

    // Getter / Setter
}