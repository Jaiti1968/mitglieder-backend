package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.service.member.MemberHonorService;
import org.springframework.web.bind.annotation.*;
import de.emc.mitglieder.dto.request.CreateMemberHonorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/members/{mitgliedsnummer}/honors")
public class MemberHonorController {

    private final MemberHonorService memberHonorService;

    public MemberHonorController(MemberHonorService memberHonorService) {
        this.memberHonorService = memberHonorService;
    }

    @GetMapping
    public List<MemberHonorDto> getHonors(
            @PathVariable String mitgliedsnummer
    ) {
        return memberHonorService.getHonors(mitgliedsnummer);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberHonorDto createHonor(
            @PathVariable String mitgliedsnummer,
            @RequestBody @Valid CreateMemberHonorRequest request
    ) {
        return memberHonorService.createHonor(mitgliedsnummer, request);
    }

    @PutMapping("/{honorId}")
    public MemberHonorDto updateHonor(
            @PathVariable String mitgliedsnummer,
            @PathVariable int honorId,
            @RequestBody @Valid CreateMemberHonorRequest request
    ) {
        return memberHonorService.updateHonor(
                mitgliedsnummer,
                honorId,
                request
        );
    }

    @DeleteMapping("/{honorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHonor(
            @PathVariable String mitgliedsnummer,
            @PathVariable int honorId
    ) {
        memberHonorService.deleteHonor(mitgliedsnummer, honorId);
    }

}

