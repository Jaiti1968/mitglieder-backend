
package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.service.member.MemberHonorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

