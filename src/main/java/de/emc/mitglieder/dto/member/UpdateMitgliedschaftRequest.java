package de.emc.mitglieder.dto.member;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateMitgliedschaftRequest(
        LocalDate eintritt,
        LocalDate austritt,

        @NotNull(message = "Mitgliederstatus muss angegeben werden")
        Integer mitgliedsstatusId,

        @NotNull(message = "Stimme muss angegeben werden")
        Integer stimmeId,

        Boolean kammerchor
) {
}