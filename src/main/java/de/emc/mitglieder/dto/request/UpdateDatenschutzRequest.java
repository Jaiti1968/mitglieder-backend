package de.emc.mitglieder.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UpdateDatenschutzRequest {

    private LocalDateTime datumDatenschutz;

    private Boolean datenschutzNr14;
    private Boolean datenschutzNr15;
    private Boolean datenschutzNr16;
    private Boolean datenschutzNr17;
    private Boolean datenschutzNr18;

    // Getter / Setter
}