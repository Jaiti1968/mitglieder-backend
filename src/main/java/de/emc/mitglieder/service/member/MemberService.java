package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.repository.LookupRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import de.emc.mitglieder.dto.member.CreateMemberRequest;
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
            Integer statusId,
            Integer stimmeId,
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

        List<MemberListItemDto> items = memberRepository.findMembers(search, statusId, stimmeId, page, pageSize);
        long totalItems = memberRepository.countMembers(search, statusId, stimmeId);
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
        memberRepository.updateStammdaten(
                mitgliedsnummer,
                request.vorname(),
                request.nachname(),
                request.ort()
        );

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    public MemberDetailDto updateKontakt(String mitgliedsnummer, UpdateKontaktRequest request) {
        memberRepository.updateKontakt(
                mitgliedsnummer,
                request.telefonPrivat(),
                request.telefonGeschaeftlich(),
                request.mobiltelefon(),
                request.email(),
                request.adresszusatz(),
                request.briefanrede()
        );

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    public MemberDetailDto updateMitgliedschaft(String mitgliedsnummer, UpdateMitgliedschaftRequest request) {

        validateMitgliedschaft(new MitgliedschaftDto(
                request.mitgliedsstatusId(),
                null,
                request.stimmeId(),
                null
        ));

        memberRepository.updateMitgliedschaft(
                mitgliedsnummer,
                request.mitgliedsstatusId(),
                request.stimmeId()
        );

        return memberRepository.findMemberById(mitgliedsnummer);
    }

    @Transactional
    public MemberDetailDto createMember(CreateMemberRequest request) {

        validateMitgliedschaft(request.mitgliedschaft());

        String currentNumber = memberRepository.getCurrentMitgliedsnummerForUpdate();
        String nextNumber = memberRepository.getNextMitgliedsnummer(currentNumber);

        memberRepository.updateNeueMitgliedsnummer(nextNumber);

        memberRepository.insertMitglied(currentNumber, request.stammdaten());
        memberRepository.insertMitgliedschaft(currentNumber, request.mitgliedschaft());
        memberRepository.insertKontakt(currentNumber, request.kontakt());
        memberRepository.insertChorkleidung(currentNumber);
        memberRepository.insertDatenschutz(currentNumber);

        return memberRepository.findMemberById(currentNumber);
    }

    private void validateMitgliedschaft(MitgliedschaftDto m) {
        if (m == null) {
            return;
        }

        if (m.mitgliedsstatusId() != null &&
                !lookupRepository.existsMemberStatus(m.mitgliedsstatusId())) {
            throw new BadRequestException("Ungültiger Mitgliederstatus");
        }

        if (m.stimmeId() != null &&
                !lookupRepository.existsVoice(m.stimmeId())) {
            throw new BadRequestException("Ungültige Stimme");
        }
    }
}