package de.emc.mitglieder.dto.member;

public record UpdateMitgliedschaftRequest(
        Integer mitgliedsstatusId,
        Integer stimmeId
) {
}