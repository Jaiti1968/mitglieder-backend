package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.service.member.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/api/members")
    public MemberListResponse getMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer stimmeId,
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
            @RequestBody UpdateStammdatenRequest request
    ) {
        return memberService.updateStammdaten(mitgliedsnummer, request);
    }

    @PutMapping("/api/members/{mitgliedsnummer}/kontakt")
    public MemberDetailDto updateKontakt(
            @PathVariable String mitgliedsnummer,
            @RequestBody UpdateKontaktRequest request
    ) {
        return memberService.updateKontakt(mitgliedsnummer, request);
    }

    @PutMapping("/api/members/{mitgliedsnummer}/mitgliedschaft")
    public MemberDetailDto updateMitgliedschaft(
            @PathVariable String mitgliedsnummer,
            @RequestBody UpdateMitgliedschaftRequest request
    ) {
        return memberService.updateMitgliedschaft(mitgliedsnummer, request);
    }
}