package de.emc.mitglieder.dto.member;

public record MitgliedschaftDto(
        Integer mitgliedsstatusId,
        String mitgliedsstatus,
        Integer stimmeId,
        String stimme
) {
}
