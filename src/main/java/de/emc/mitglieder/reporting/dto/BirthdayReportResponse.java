package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayReportResponse {

    private Integer geschaeftsjahr;

    private List<BirthdayReportMonthGroupDto> monthGroups;

    private Instant generatedAt;
}
