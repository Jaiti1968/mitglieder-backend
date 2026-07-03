package de.emc.mitglieder.reporting.service;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportGroupDto;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportMemberDto;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportRequest;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportSubGroupDto;
import de.emc.mitglieder.reporting.repository.MembersDirectoryReportRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembersDirectoryReportService {

    private final MembersDirectoryReportRepository membersDirectoryReportRepository;

    public MembersDirectoryReportService(
            MembersDirectoryReportRepository membersDirectoryReportRepository
    ) {
        this.membersDirectoryReportRepository = membersDirectoryReportRepository;
    }

    public MembersDirectoryReportResponse createReport(MembersDirectoryReportRequest request) {

        List<MembersDirectoryReportMemberDto> members =
                membersDirectoryReportRepository.findMembersDirectory(
                        request.getSearch(),
                        request.getMitgliedsstatusIds(),
                        request.getStimmeIds()
                );

        List<MembersDirectoryReportMemberDto> reportMembers = members.stream()
                .filter(member -> request.getKammerchor() == null
                        || !request.getKammerchor()
                        || Boolean.TRUE.equals(member.getKammerchor()))
                .sorted(
                        Comparator
                                .comparing(
                                        MembersDirectoryReportMemberDto::getMitgliedsstatusId,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .thenComparing(
                                        MembersDirectoryReportMemberDto::getStimmeId,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .thenComparing(
                                        MembersDirectoryReportMemberDto::getNachname,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                                )
                                .thenComparing(
                                        MembersDirectoryReportMemberDto::getVorname,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                                )
                )
                .toList();

        List<MembersDirectoryReportGroupDto> groups = reportMembers.stream()
                .collect(Collectors.groupingBy(
                        member -> member.getMitgliedsstatus() != null
                                ? member.getMitgliedsstatus()
                                : "Ohne Status",
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                member -> member.getStimme() != null
                                        ? member.getStimme()
                                        : "Ohne Stimme",
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(statusEntry -> new MembersDirectoryReportGroupDto(
                        statusEntry.getKey(),
                        statusEntry.getValue()
                                .entrySet()
                                .stream()
                                .map(voiceEntry -> new MembersDirectoryReportSubGroupDto(
                                        voiceEntry.getKey(),
                                        voiceEntry.getValue()
                                ))
                                .toList()
                ))
                .toList();

        return new MembersDirectoryReportResponse(
                groups,
                Instant.now()
        );
    }
}