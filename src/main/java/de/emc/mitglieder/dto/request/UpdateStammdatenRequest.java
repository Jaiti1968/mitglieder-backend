package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateStammdatenRequest {

    @NotNull(message = "Person/Firma muss angegeben werden")
    private Boolean personFirma;

    @Size(max = 50, message = "Anrede darf maximal 50 Zeichen haben")
    private String anrede;

    @Size(max = 50, message = "Akademischer Titel darf maximal 50 Zeichen haben")
    private String akademischerTitel;

    @Size(max = 50, message = "Vorname darf maximal 50 Zeichen haben")
    private String vorname;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(max = 50, message = "Nachname darf maximal 50 Zeichen haben")
    private String nachname;

    @Size(max = 50, message = "PLZ darf maximal 50 Zeichen haben")
    private String plz;

    @Size(max = 50, message = "Ort darf maximal 50 Zeichen haben")
    private String ort;

    @Size(max = 50, message = "Straße/Hausnummer darf maximal 50 Zeichen haben")
    private String strasseHausNr;

    private LocalDate geburtsdatum;

}