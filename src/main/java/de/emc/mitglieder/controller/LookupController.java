package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.LookupItemDto;
import de.emc.mitglieder.service.LookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/member-status")
    public List<LookupItemDto> getMemberStatus() {
        return lookupService.getMemberStatus();
    }

    @GetMapping("/voices")
    public List<LookupItemDto> getVoices() {
        return lookupService.getVoices();
    }
}