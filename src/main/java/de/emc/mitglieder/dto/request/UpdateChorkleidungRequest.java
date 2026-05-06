package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateChorkleidungRequest {

    @Size(max = 50)
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

    @DecimalMin(value = "0.00", message = "Kaufpreis darf nicht negativ sein")
    @Digits(integer = 8, fraction = 2, message = "Kaufpreis hat falsches Format")
    private BigDecimal kaufpreis;

    private Boolean sommerkleidung;

    private LocalDateTime sommerkleidungErhalten;

    private LocalDateTime sommerkleidungRueckgabe;
}