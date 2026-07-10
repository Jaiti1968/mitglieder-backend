package de.emc.mitglieder.reporting.service;

import de.emc.mitglieder.reporting.dto.BirthdayReportMemberDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportMonthGroupDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportRequest;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import de.emc.mitglieder.reporting.repository.BirthdayReportMemberData;
import de.emc.mitglieder.reporting.repository.BirthdayReportRepository;
import de.emc.mitglieder.reporting.repository.ReportingSettingsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BirthdayReportService {

    private static final int MINIMUM_JUBILEE_AGE = 60;

    private final BirthdayReportRepository birthdayReportRepository;
    private final ReportingSettingsRepository reportingSettingsRepository;
    private final YearBasedCalculationService yearBasedCalculationService;

    public BirthdayReportService(
            BirthdayReportRepository birthdayReportRepository,
            ReportingSettingsRepository reportingSettingsRepository,
            YearBasedCalculationService yearBasedCalculationService
    ) {
        this.birthdayReportRepository = birthdayReportRepository;
        this.reportingSettingsRepository = reportingSettingsRepository;
        this.yearBasedCalculationService = yearBasedCalculationService;
    }

    public BirthdayReportResponse createReport(
            BirthdayReportRequest request
    ) {
        BirthdayReportRequest effectiveRequest =
                request != null ? request : new BirthdayReportRequest();

        int businessYear =
                reportingSettingsRepository.findCurrentBusinessYear();

        List<BirthdayReportMemberData> memberData =
                birthdayReportRepository.findBirthdayReportMembers(
                        effectiveRequest.getMitgliedsstatusIds(),
                        effectiveRequest.getStimmeIds()
                );

        Map<Integer, List<BirthdayReportMemberDto>> membersByMonth =
                new TreeMap<>();

        List<BirthdayReportMemberDto> membersWithoutBirthday =
                new ArrayList<>();

        for (BirthdayReportMemberData member : memberData) {
            BirthdayReportMemberDto memberDto =
                    createMemberDto(member, businessYear);

            if (member.getGeburtsdatum() == null) {
                membersWithoutBirthday.add(memberDto);
                continue;
            }

            int month = member.getGeburtsdatum().getMonthValue();

            membersByMonth
                    .computeIfAbsent(month, ignored -> new ArrayList<>())
                    .add(memberDto);
        }

        List<BirthdayReportMonthGroupDto> monthGroups =
                new ArrayList<>();

        for (Map.Entry<Integer, List<BirthdayReportMemberDto>> entry
                : membersByMonth.entrySet()) {

            int month = entry.getKey();

            monthGroups.add(
                    new BirthdayReportMonthGroupDto(
                            month,
                            getGermanMonthName(month),
                            entry.getValue()
                    )
            );
        }

        if (!membersWithoutBirthday.isEmpty()) {
            monthGroups.add(
                    new BirthdayReportMonthGroupDto(
                            null,
                            "Unbekannt",
                            membersWithoutBirthday
                    )
            );
        }

        return new BirthdayReportResponse(
                businessYear,
                monthGroups,
                Instant.now()
        );
    }

    private BirthdayReportMemberDto createMemberDto(
            BirthdayReportMemberData member,
            int businessYear
    ) {
        Integer age =
                yearBasedCalculationService.calculateYearsReachedInYear(
                        member.getGeburtsdatum(),
                        businessYear
                );

        boolean jubileeBirthday =
                yearBasedCalculationService.isFiveYearMilestoneFrom(
                        age,
                        MINIMUM_JUBILEE_AGE
                );

        return new BirthdayReportMemberDto(
                member.getMitgliedsnummer(),
                member.getVorname(),
                member.getNachname(),
                member.getMitgliedsstatusId(),
                member.getMitgliedsstatus(),
                member.getStimmeId(),
                member.getStimme(),
                member.getGeburtsdatum(),
                age,
                jubileeBirthday
        );
    }

    private String getGermanMonthName(int month) {
        return Month.of(month)
                .getDisplayName(
                        TextStyle.FULL,
                        Locale.GERMAN
                );
    }
}