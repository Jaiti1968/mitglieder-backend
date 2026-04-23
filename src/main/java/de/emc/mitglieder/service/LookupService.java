package de.emc.mitglieder.service;

import de.emc.mitglieder.dto.LookupItemDto;
import de.emc.mitglieder.repository.LookupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LookupService {

    private final LookupRepository lookupRepository;

    public LookupService(LookupRepository lookupRepository) {
        this.lookupRepository = lookupRepository;
    }

    public List<LookupItemDto> getMemberStatus() {
        return lookupRepository.findMemberStatus();
    }

    public List<LookupItemDto> getVoices() {
        return lookupRepository.findVoices();
    }
}