package de.emc.mitglieder.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KontaktDto {

    private String telefonPrivat;
    private String telefonGeschaeftlich;
    private String mobiltelefon;
    private String email;
    private String adresszusatz;
    private String briefanrede;

}