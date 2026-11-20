package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberHonorDto {

    private Integer honorId;
    private String mitgliedsnummer;
    private Integer honorTypeId;
    private String honorName;
    private LocalDate honorDate;
}