package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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

    public void updateStammdaten(String mitgliedsnummer, UpdateStammdatenRequest request) {
        memberRepository.updateStammdaten(
                mitgliedsnummer,
                request.vorname(),
                request.nachname(),
                request.ort()
        );
    }

    public void updateKontakt(String mitgliedsnummer, UpdateKontaktRequest request) {
        memberRepository.updateKontakt(
                mitgliedsnummer,
                request.telefonPrivat(),
                request.telefonGeschaeftlich(),
                request.mobiltelefon(),
                request.email(),
                request.adresszusatz(),
                request.briefanrede()
        );
    }
}