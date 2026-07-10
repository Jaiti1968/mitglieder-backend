package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayReportMonthGroupDto {

    private Integer monat;

    private String monatsname;

    private List<BirthdayReportMemberDto> mitglieder;
}