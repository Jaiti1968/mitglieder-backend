package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StammdatenDto {

    private String anrede;
    private String akademischerTitel;
    private String vorname;
    private String nachname;
    private String plz;
    private String ort;
    private String strasseHausNr;
    private    LocalDate geburtsdatum;

}