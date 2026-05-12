package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDatenschutzDto {

    private String mitgliedsnummer;
    private LocalDate datumDatenschutz;

    private boolean datenschutzNr14;
    private boolean datenschutzNr15;
    private boolean datenschutzNr16;
    private boolean datenschutzNr17;
    private boolean datenschutzNr18;

    // Getter / Setter
}