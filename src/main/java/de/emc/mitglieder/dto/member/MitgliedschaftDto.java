package de.emc.mitglieder.dto.member;

import java.time.LocalDate;

public record MitgliedschaftDto(
        LocalDate eintritt,
        LocalDate austritt,
        Integer mitgliedsstatusId,
        String mitgliedsstatus,
        Integer stimmeId,
        String stimme,
        Boolean kammerchor
) {
}
