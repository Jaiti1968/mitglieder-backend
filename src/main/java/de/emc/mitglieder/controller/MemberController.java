package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.dto.request.UpdateKontaktRequest;
import de.emc.mitglieder.dto.request.UpdateMitgliedschaftRequest;
import de.emc.mitglieder.dto.request.UpdateStammdatenRequest;
import de.emc.mitglieder.service.member.MemberService;
import org.springframework.web.bind.annotation.*;
import de.emc.mitglieder.dto.request.CreateMemberRequest;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

import java.util.List;

@SuppressWarnings("unused")
@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/api/members")
    public MemberListResponse getMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> statusId,
            @RequestParam(required = false) List<Integer> stimmeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return memberService.getMembers(search, statusId, stimmeId, page, pageSize);
    }

    @GetMapping("/api/members/{mitgliedsnummer}")
    public MemberDetailDto getMemberById(@PathVariable String mitgliedsnummer) {
        return memberService.getMemberById(mitgliedsnummer);
    }

    @PutMapping("/api/members/{mitgliedsnummer}/stammdaten")
    public MemberDetailDto updateStammdaten(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateStammdatenRequest request
    ) {
        return memberService.updateStammdaten(mitgliedsnummer, request);
    }

    @PutMapping("/api/members/{mitgliedsnummer}/kontakt")
    public MemberDetailDto updateKontakt(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateKontaktRequest request
    ) {
        return memberService.updateKontakt(mitgliedsnummer, request);
    }

    @PutMapping("/api/members/{mitgliedsnummer}/mitgliedschaft")
    public MemberDetailDto updateMitgliedschaft(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateMitgliedschaftRequest request
    ) {
        return memberService.updateMitgliedschaft(mitgliedsnummer, request);
    }

    @PostMapping("/api/members")
    public MemberDetailDto createMember(@RequestBody CreateMemberRequest request) {
        return memberService.createMember(request);
    }
}