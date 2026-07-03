package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembersDirectoryReportResponse {

    private List<MembersDirectoryReportGroupDto> groups;

    private Instant generatedAt;
}
