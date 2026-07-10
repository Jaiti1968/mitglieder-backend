package de.emc.mitglieder.reporting.service;

import de.emc.mitglieder.reporting.dto.BirthdayReportMemberDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportMonthGroupDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportRequest;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import de.emc.mitglieder.reporting.repository.BirthdayReportMemberData;
import de.emc.mitglieder.reporting.repository.BirthdayReportRepository;
import de.emc.mitglieder.reporting.repository.ReportingSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BirthdayReportServiceTest {

    @Mock
    private BirthdayReportRepository birthdayReportRepository;

    @Mock
    private ReportingSettingsRepository reportingSettingsRepository;

    private BirthdayReportService birthdayReportService;

    @BeforeEach
    void setUp() {
        birthdayReportService = new BirthdayReportService(
                birthdayReportRepository,
                reportingSettingsRepository,
                new YearBasedCalculationService()
        );
    }

    @Test
    void shouldCreateBirthdayReportWithBusinessYear() {
        BirthdayReportRequest request = new BirthdayReportRequest();
        request.setMitgliedsstatusIds(List.of(1));
        request.setStimmeIds(List.of(2));

        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        List.of(1),
                        List.of(2)
                )
        ).thenReturn(List.of());

        BirthdayReportResponse response =
                birthdayReportService.createReport(request);

        assertEquals(2026, response.getGeschaeftsjahr());
        assertTrue(response.getMonthGroups().isEmpty());
        assertTrue(response.getGeneratedAt() != null);

        verify(birthdayReportRepository)
                .findBirthdayReportMembers(
                        List.of(1),
                        List.of(2)
                );
    }

    @Test
    void shouldCalculateAgeReachedInBusinessYear() {
        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        null,
                        null
                )
        ).thenReturn(List.of(
                createMember(
                        "1001",
                        "Max",
                        "Mustermann",
                        LocalDate.of(1980, 7, 12)
                )
        ));

        BirthdayReportResponse response =
                birthdayReportService.createReport(
                        new BirthdayReportRequest()
                );

        BirthdayReportMemberDto member =
                response.getMonthGroups()
                        .get(0)
                        .getMitglieder()
                        .get(0);

        assertEquals(46, member.getAlterImGeschaeftsjahr());
        assertFalse(member.getJubilaeumsgeburtstag());
    }

    @Test
    void shouldMarkJubileeBirthdayFromAgeSixty() {
        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        null,
                        null
                )
        ).thenReturn(List.of(
                createMember(
                        "1001",
                        "Anna",
                        "Beispiel",
                        LocalDate.of(1966, 3, 10)
                ),
                createMember(
                        "1002",
                        "Berta",
                        "Beispiel",
                        LocalDate.of(1961, 3, 20)
                ),
                createMember(
                        "1003",
                        "Clara",
                        "Beispiel",
                        LocalDate.of(1965, 3, 30)
                )
        ));

        BirthdayReportResponse response =
                birthdayReportService.createReport(
                        new BirthdayReportRequest()
                );

        List<BirthdayReportMemberDto> members =
                response.getMonthGroups()
                        .get(0)
                        .getMitglieder();

        assertEquals(60, members.get(0).getAlterImGeschaeftsjahr());
        assertTrue(members.get(0).getJubilaeumsgeburtstag());

        assertEquals(65, members.get(1).getAlterImGeschaeftsjahr());
        assertTrue(members.get(1).getJubilaeumsgeburtstag());

        assertEquals(61, members.get(2).getAlterImGeschaeftsjahr());
        assertFalse(members.get(2).getJubilaeumsgeburtstag());
    }

    @Test
    void shouldGroupMembersByMonth() {
        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        null,
                        null
                )
        ).thenReturn(List.of(
                createMember(
                        "1001",
                        "Max",
                        "Januar",
                        LocalDate.of(1980, 1, 15)
                ),
                createMember(
                        "1002",
                        "Maria",
                        "März",
                        LocalDate.of(1985, 3, 10)
                )
        ));

        BirthdayReportResponse response =
                birthdayReportService.createReport(
                        new BirthdayReportRequest()
                );

        assertEquals(2, response.getMonthGroups().size());

        assertEquals(1, response.getMonthGroups().get(0).getMonat());
        assertEquals(
                "Januar",
                response.getMonthGroups().get(0).getMonatsname()
        );

        assertEquals(3, response.getMonthGroups().get(1).getMonat());
        assertEquals(
                "März",
                response.getMonthGroups().get(1).getMonatsname()
        );
    }

    @Test
    void shouldPlaceMissingBirthdaysInUnknownGroup() {
        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        null,
                        null
                )
        ).thenReturn(List.of(
                createMember(
                        "1001",
                        "Max",
                        "Bekannt",
                        LocalDate.of(1980, 1, 15)
                ),
                createMember(
                        "1002",
                        "Erika",
                        "Unbekannt",
                        null
                )
        ));

        BirthdayReportResponse response =
                birthdayReportService.createReport(
                        new BirthdayReportRequest()
                );

        assertEquals(2, response.getMonthGroups().size());

        BirthdayReportMonthGroupDto unknownGroup =
                response.getMonthGroups().get(1);

        assertNull(unknownGroup.getMonat());
        assertEquals("Unbekannt", unknownGroup.getMonatsname());
        assertEquals(1, unknownGroup.getMitglieder().size());

        BirthdayReportMemberDto member =
                unknownGroup.getMitglieder().get(0);

        assertNull(member.getGeburtsdatum());
        assertNull(member.getAlterImGeschaeftsjahr());
        assertFalse(member.getJubilaeumsgeburtstag());
    }

    @Test
    void shouldAcceptNullRequest() {
        when(reportingSettingsRepository.findCurrentBusinessYear())
                .thenReturn(2026);

        when(
                birthdayReportRepository.findBirthdayReportMembers(
                        null,
                        null
                )
        ).thenReturn(List.of());

        BirthdayReportResponse response =
                birthdayReportService.createReport(null);

        assertEquals(2026, response.getGeschaeftsjahr());
        assertTrue(response.getMonthGroups().isEmpty());
    }

    private BirthdayReportMemberData createMember(
            String mitgliedsnummer,
            String vorname,
            String nachname,
            LocalDate geburtsdatum
    ) {
        return new BirthdayReportMemberData(
                mitgliedsnummer,
                vorname,
                nachname,
                1,
                "Aktiv",
                2,
                "Tenor",
                geburtsdatum
        );
    }
}
