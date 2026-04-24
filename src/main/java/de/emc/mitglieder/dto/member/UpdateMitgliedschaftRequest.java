package de.emc.mitglieder.dto.member;

import jakarta.validation.constraints.NotNull;

public record UpdateMitgliedschaftRequest(
        @NotNull(message = "Mitgliederstatus muss angegeben werden")
        Integer mitgliedsstatusId,

        @NotNull(message = "Stimme muss angegeben werden")
        Integer stimmeId
) {
}