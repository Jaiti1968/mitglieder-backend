package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateKontaktRequest {

    @Size(max = 50, message = "Telefon privat darf maximal 50 Zeichen haben")
    private String telefonPrivat;

    @Size(max = 50, message = "Telefon geschäftlich darf maximal 50 Zeichen haben")
    private String telefonGeschaeftlich;

    @Size(max = 50, message = "Mobiltelefon darf maximal 50 Zeichen haben")
    private String mobiltelefon;

    @Email(message = "E-Mail muss gültig sein")
    @Size(max = 100, message = "E-Mail darf maximal 100 Zeichen haben")
    private String email;

    @Size(max = 50, message = "Adresszusatz darf maximal 50 Zeichen haben")
    private String adresszusatz;

    @Size(max = 100, message = "Briefanrede darf maximal 100 Zeichen haben")
    private String briefanrede;

}