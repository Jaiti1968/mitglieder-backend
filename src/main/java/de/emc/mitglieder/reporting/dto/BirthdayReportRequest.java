package de.emc.mitglieder.reporting.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BirthdayReportRequest {

    private List<Integer> mitgliedsstatusIds;
    private List<Integer> stimmeIds;

}
