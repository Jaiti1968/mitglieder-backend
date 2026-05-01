package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateMitgliedschaftRequest {

    private LocalDate eintritt;
    private LocalDate austritt;

    @NotNull(message = "Mitgliederstatus muss angegeben werden")
    private Integer mitgliedsstatusId;

    @NotNull(message = "Stimme muss angegeben werden")
    private Integer stimmeId;

    private Boolean kammerchor;

}