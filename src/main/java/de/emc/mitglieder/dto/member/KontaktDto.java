package de.emc.mitglieder.dto.member;

public record KontaktDto(
        String telefonPrivat,
        String telefonGeschaeftlich,
        String mobiltelefon,
        String email,
        String adresszusatz,
        String briefanrede
) {
}