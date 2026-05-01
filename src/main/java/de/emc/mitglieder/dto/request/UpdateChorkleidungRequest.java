package de.emc.mitglieder.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateChorkleidungRequest {

    private String ehemaligeStimme;
    private LocalDateTime uebergabeAm;
    private String bemerkungUebergabe;

    private Boolean neubeschaffung;
    private LocalDateTime datumAnteil;
    private Boolean barzahlung;

    private String bearbeitungsstand;

    private LocalDateTime rueckgabeAm;
    private String bemerkungRueckgabe;

    private LocalDateTime kaufdatum;
    private BigDecimal kaufpreis;

    private Boolean sommerkleidung;
    private LocalDateTime sommerkleidungErhalten;
    private LocalDateTime sommerkleidungRueckgabe;

    // Getter / Setter
}
