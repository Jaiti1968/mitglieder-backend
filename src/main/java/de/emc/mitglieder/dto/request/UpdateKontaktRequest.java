package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateKontaktRequest(
        @Size(max = 50, message = "Telefon privat darf maximal 50 Zeichen haben")
        String telefonPrivat,

        @Size(max = 50, message = "Telefon geschäftlich darf maximal 50 Zeichen haben")
        String telefonGeschaeftlich,

        @Size(max = 50, message = "Mobiltelefon darf maximal 50 Zeichen haben")
        String mobiltelefon,

        @Email(message = "E-Mail muss gültig sein")
        @Size(max = 50, message = "E-Mail darf maximal 50 Zeichen haben")
        String email,

        @Size(max = 30, message = "Adresszusatz darf maximal 30 Zeichen haben")
        String adresszusatz,

        @Size(max = 100, message = "Briefanrede darf maximal 100 Zeichen haben")
        String briefanrede
) {
}