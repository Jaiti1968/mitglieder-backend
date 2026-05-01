package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.dto.request.*;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.repository.LookupRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import de.emc.mitglieder.dto.request.CreateMemberRequest;
import org.springframework.transaction.annotation.Transactional;
import de.emc.mitglieder.exception.BadRequestException;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final LookupRepository lookupRepository;

    public MemberService(MemberRepository memberRepository, LookupRepository lookupRepository) {
        this.memberRepository = memberRepository;
        this.lookupRepository = lookupRepository;
    }

    public MemberListResponse getMembers(
            String search,
            List<Integer> statusIds,
            List<Integer> stimmeIds,
            int page,
            int pageSize
    ) {
        if (page < 1) {
            page = 1;
        }

        if (pageSize < 1) {
            pageSize = 20;
        }

        if (pageSize > 100) {
            pageSize = 100;
        }

        List<MemberListItemDto> items =
                memberRepository.findMembers(search, statusIds, stimmeIds, page, pageSize);
        long totalItems =
                memberRepository.countMembers(search, statusIds, stimmeIds);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        PaginationDto pagination = new PaginationDto(
                page,
                pageSize,
                totalItems,
                totalPages
        );

        return new MemberListResponse(items, pagination);
    }

    public MemberDetailDto getMemberById(String mitgliedsnummer) {
        return memberRepository.findMemberById(mitgliedsnummer);
    }

    public MemberDetailDto updateStammdaten(String mitgliedsnummer, UpdateStammdatenRequest request) {
        memberRepository.updateStammdaten(mitgliedsnummer, request);

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    public MemberDetailDto updateKontakt(String mitgliedsnummer, UpdateKontaktRequest request) {
        memberRepository.updateKontakt(
                mitgliedsnummer,
                request.getTelefonPrivat(),
                request.getTelefonGeschaeftlich(),
                request.getMobiltelefon(),
                request.getEmail(),
                request.getAdresszusatz(),
                request.getBriefanrede()
        );

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    public MemberDetailDto updateMitgliedschaft(String mitgliedsnummer, UpdateMitgliedschaftRequest request) {

        validateMitgliedschaft(new MitgliedschaftDto(
                null,
                null,
                request.getMitgliedsstatusId(),
                null,
                request.getStimmeId(),
                null,
                null
        ));

        memberRepository.updateMitgliedschaft(mitgliedsnummer, request);

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    @Transactional
    public MemberDetailDto createMember(CreateMemberRequest request) {

        validateMitgliedschaft(request.getMitgliedschaft());

        String currentNumber = memberRepository.getCurrentMitgliedsnummerForUpdate();
        String nextNumber = memberRepository.getNextMitgliedsnummer(currentNumber);

        memberRepository.updateNeueMitgliedsnummer(nextNumber);

        memberRepository.insertMitglied(currentNumber, request.getStammdaten());
        memberRepository.insertMitgliedschaft(currentNumber, request.getMitgliedschaft());
        memberRepository.insertKontakt(currentNumber, request.getKontakt());
        memberRepository.insertChorkleidung(currentNumber);
        memberRepository.insertDatenschutz(currentNumber);

        return memberRepository.findMemberById(currentNumber);
    }

    private void validateMitgliedschaft(MitgliedschaftDto m) {
        if (m == null) {
            return;
        }

        if (m.getMitgliedsstatusId() != null &&
                !lookupRepository.existsMemberStatus(m.getMitgliedsstatusId())) {
            throw new BadRequestException("Ungültiger Mitgliederstatus");
        }

        if (m.getStimmeId() != null &&
                !lookupRepository.existsVoice(m.getStimmeId())) {
            throw new BadRequestException("Ungültige Stimme");
        }
    }

    public MemberDatenschutzDto getDatenschutz(String mitgliedsnummer) {
        return memberRepository.findDatenschutzByMitgliedsnummer(mitgliedsnummer)
                .orElseThrow(() -> new NotFoundException("Datenschutz nicht gefunden für Mitglied " + mitgliedsnummer));
    }

    public MemberDatenschutzDto updateDatenschutz(String mitgliedsnummer, UpdateDatenschutzRequest request) {

        int updatedRows = memberRepository.updateDatenschutz(mitgliedsnummer, request);

        if (updatedRows == 0) {
            throw new NotFoundException("Datenschutz nicht gefunden für Mitglied " + mitgliedsnummer);
        }

        return getDatenschutz(mitgliedsnummer);
    }

    public MemberChorkleidungDto getChorkleidung(String mitgliedsnummer) {
        return memberRepository.findChorkleidungByMitgliedsnummer(mitgliedsnummer)
                .orElseThrow(() -> new NotFoundException("Chorkleidung nicht gefunden für Mitglied " + mitgliedsnummer));
    }

    public MemberChorkleidungDto updateChorkleidung(String mitgliedsnummer, UpdateChorkleidungRequest request) {

        int updatedRows = memberRepository.updateChorkleidung(mitgliedsnummer, request);

        if (updatedRows == 0) {
            throw new NotFoundException("Chorkleidung nicht gefunden für Mitglied " + mitgliedsnummer);
        }

        return getChorkleidung(mitgliedsnummer);
    }
}