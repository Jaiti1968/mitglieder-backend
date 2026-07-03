package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembersDirectoryReportRequest {

    private List<Integer> mitgliedsstatusIds;
    private List<Integer> stimmeIds;
    private Boolean kammerchor;
    private String search;
}
