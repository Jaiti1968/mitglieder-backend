package de.emc.mitglieder.reporting.service;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportRequest;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import de.emc.mitglieder.reporting.repository.MembersDirectoryReportRepository;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportMemberDto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MembersDirectoryReportServiceTest {

    private final MembersDirectoryReportRepository membersDirectoryReportRepository =
            mock(MembersDirectoryReportRepository.class);

    private final MembersDirectoryReportService service =
            new MembersDirectoryReportService(membersDirectoryReportRepository);

    @Test
    void createReport_shouldGroupAndSortMembersByStatusVoiceAndName() {
        when(membersDirectoryReportRepository.findMembersDirectory(null, null, null))
                .thenReturn(List.of(
                        new MembersDirectoryReportMemberDto("N0003", "Berta", "Zimmer", null, null, null, null, null, null, null, null, 5, "Ehrenmitglied", 6, "keine", false),
                        new MembersDirectoryReportMemberDto("N0002", "Anton", "Meyer", null, null, null, null, null, null, null, null, 1, "Aktives Mitglied", 4, "2. Tenor", false),
                        new MembersDirectoryReportMemberDto("N0001", "Carl", "Schmidt", null, null, null, null, null, null, null, null, 1, "Aktives Mitglied", 1, "1. Bass", true),
                        new MembersDirectoryReportMemberDto("N0004", "Anna", "Bauer", null, null, null, null, null, null, null, null, 1, "Aktives Mitglied", 1, "1. Bass", false)
                ));

        MembersDirectoryReportResponse response =
                service.createReport(new MembersDirectoryReportRequest(null, null, null, null));

        assertThat(response.getGroups()).hasSize(2);

        assertThat(response.getGroups().get(0).getMitgliedsstatus())
                .isEqualTo("Aktives Mitglied");

        assertThat(response.getGroups().get(0).getStimmgruppen())
                .hasSize(2);

        assertThat(response.getGroups().get(0).getStimmgruppen().get(0).getStimme())
                .isEqualTo("1. Bass");

        assertThat(response.getGroups().get(0).getStimmgruppen().get(0).getMitglieder())
                .extracting("nachname")
                .containsExactly("Bauer", "Schmidt");

        assertThat(response.getGroups().get(0).getStimmgruppen().get(1).getStimme())
                .isEqualTo("2. Tenor");

        assertThat(response.getGroups().get(1).getMitgliedsstatus())
                .isEqualTo("Ehrenmitglied");

        verify(membersDirectoryReportRepository)
                .findMembersDirectory(null, null, null);
    }

    @Test
    void createReport_shouldFilterChamberChoirMembers() {
        when(membersDirectoryReportRepository.findMembersDirectory(null, null, null))
                .thenReturn(List.of(
                        new MembersDirectoryReportMemberDto("N0001", "Carl", "Schmidt", null, null, null, null, null, null, null, null, 1, "Aktives Mitglied", 1, "1. Bass", true),
                        new MembersDirectoryReportMemberDto("N0002", "Anna", "Bauer", null, null, null, null, null, null, null, null, 1, "Aktives Mitglied", 1, "1. Bass", false)
                ));

        MembersDirectoryReportRequest request =
                new MembersDirectoryReportRequest(null, null, true, null);

        MembersDirectoryReportResponse response = service.createReport(request);

        assertThat(response.getGroups()).hasSize(1);
        assertThat(response.getGroups().get(0).getStimmgruppen().get(0).getMitglieder())
                .hasSize(1);

        assertThat(response.getGroups().get(0).getStimmgruppen().get(0).getMitglieder().get(0).getMitgliedsnummer())
                .isEqualTo("N0001");
    }
}