package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.member.MemberChorkleidungDto;
import de.emc.mitglieder.dto.member.MemberDatenschutzDto;
import de.emc.mitglieder.dto.member.MemberDetailDto;
import de.emc.mitglieder.dto.member.MemberListResponse;
import de.emc.mitglieder.dto.request.CreateMemberRequest;
import de.emc.mitglieder.dto.request.UpdateChorkleidungRequest;
import de.emc.mitglieder.dto.request.UpdateDatenschutzRequest;
import de.emc.mitglieder.dto.request.UpdateKontaktRequest;
import de.emc.mitglieder.dto.request.UpdateMitgliedschaftRequest;
import de.emc.mitglieder.dto.request.UpdateStammdatenRequest;
import de.emc.mitglieder.service.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public MemberListResponse getMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> statusId,
            @RequestParam(required = false) List<Integer> stimmeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return memberService.getMembers(search, statusId, stimmeId, page, pageSize);
    }

    @GetMapping("/{mitgliedsnummer}")
    public MemberDetailDto getMemberById(@PathVariable String mitgliedsnummer) {
        return memberService.getMemberById(mitgliedsnummer);
    }

    @PostMapping
    public MemberDetailDto createMember(@RequestBody @Valid CreateMemberRequest request) {
        return memberService.createMember(request);
    }

    @PutMapping("/{mitgliedsnummer}/stammdaten")
    public MemberDetailDto updateStammdaten(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateStammdatenRequest request
    ) {
        return memberService.updateStammdaten(mitgliedsnummer, request);
    }

    @PutMapping("/{mitgliedsnummer}/kontakt")
    public MemberDetailDto updateKontakt(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateKontaktRequest request
    ) {
        return memberService.updateKontakt(mitgliedsnummer, request);
    }

    @PutMapping("/{mitgliedsnummer}/mitgliedschaft")
    public MemberDetailDto updateMitgliedschaft(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateMitgliedschaftRequest request
    ) {
        return memberService.updateMitgliedschaft(mitgliedsnummer, request);
    }

    @GetMapping("/{mitgliedsnummer}/datenschutz")
    public MemberDatenschutzDto getDatenschutz(@PathVariable String mitgliedsnummer) {
        return memberService.getDatenschutz(mitgliedsnummer);
    }

    @PutMapping("/{mitgliedsnummer}/datenschutz")
    public MemberDatenschutzDto updateDatenschutz(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateDatenschutzRequest request
    ) {
        return memberService.updateDatenschutz(mitgliedsnummer, request);
    }

    @GetMapping("/{mitgliedsnummer}/chorkleidung")
    public MemberChorkleidungDto getChorkleidung(@PathVariable String mitgliedsnummer) {
        return memberService.getChorkleidung(mitgliedsnummer);
    }

    @PutMapping("/{mitgliedsnummer}/chorkleidung")
    public MemberChorkleidungDto updateChorkleidung(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid UpdateChorkleidungRequest request
    ) {
        return memberService.updateChorkleidung(mitgliedsnummer, request);
    }
}