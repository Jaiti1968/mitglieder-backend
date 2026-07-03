package de.emc.mitglieder.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembersDirectoryReportSubGroupDto {

    private String stimme;

    private List<MembersDirectoryReportMemberDto> mitglieder;
}
