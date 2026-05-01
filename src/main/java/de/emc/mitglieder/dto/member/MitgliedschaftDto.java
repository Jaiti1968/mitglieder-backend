package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MitgliedschaftDto {

    private LocalDate eintritt;
    private LocalDate austritt;
    private Integer mitgliedsstatusId;
    private String mitgliedsstatus;
    private Integer stimmeId;
    private String stimme;
    private Boolean kammerchor;

}
