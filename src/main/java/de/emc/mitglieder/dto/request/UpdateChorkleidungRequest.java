package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class UpdateChorkleidungRequest {

    @Size(max = 50)
    private String ehemaligeStimme;

    private LocalDate uebergabeAm;

    private String bemerkungUebergabe;

    private Boolean neubeschaffung;

    private LocalDate datumAnteil;

    private Boolean barzahlung;

    private String bearbeitungsstand;

    private LocalDate rueckgabeAm;

    private String bemerkungRueckgabe;

    private LocalDate kaufdatum;

    @DecimalMin(value = "0.00", message = "Kaufpreis darf nicht negativ sein")
    @Digits(integer = 8, fraction = 2, message = "Kaufpreis hat falsches Format")
    private BigDecimal kaufpreis;

    private Boolean sommerkleidung;

    private LocalDate sommerkleidungErhalten;

    private LocalDate sommerkleidungRueckgabe;
}