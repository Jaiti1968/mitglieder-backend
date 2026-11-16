
package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberHonorService {

    private final MemberHonorRepository memberHonorRepository;
    private final MemberRepository memberRepository;

    public MemberHonorService(
            MemberHonorRepository memberHonorRepository,
            MemberRepository memberRepository
    ) {
        this.memberHonorRepository = memberHonorRepository;
        this.memberRepository = memberRepository;
    }

    public List<MemberHonorDto> getHonors(String mitgliedsnummer) {
        if (!memberRepository.existsByMitgliedsnummer(mitgliedsnummer)) {
            throw new NotFoundException(
                    "Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden."
            );
        }

        return memberHonorRepository.findByMitgliedsnummer(mitgliedsnummer);
    }
}
