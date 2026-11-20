package de.emc.mitglieder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMemberHonorRequest {

    @NotNull
    private Integer honorTypeId;

    private LocalDate honorDate;
}
