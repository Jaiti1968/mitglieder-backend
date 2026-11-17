package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import de.emc.mitglieder.dto.request.CreateMemberHonorRequest;
import de.emc.mitglieder.exception.BadRequestException;
import de.emc.mitglieder.repository.LookupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberHonorService {

    private final MemberHonorRepository memberHonorRepository;
    private final MemberRepository memberRepository;
    private final LookupRepository lookupRepository;

    public MemberHonorService(
            MemberHonorRepository memberHonorRepository,
            MemberRepository memberRepository,
            LookupRepository lookupRepository
    ) {
        this.memberHonorRepository = memberHonorRepository;
        this.memberRepository = memberRepository;
        this.lookupRepository = lookupRepository;
    }

    public List<MemberHonorDto> getHonors(String mitgliedsnummer) {
        if (!memberRepository.existsByMitgliedsnummer(mitgliedsnummer)) {
            throw new NotFoundException(
                    "Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden."
            );
        }

        return memberHonorRepository.findByMitgliedsnummer(mitgliedsnummer);
    }

    public MemberHonorDto createHonor(
            String mitgliedsnummer,
            CreateMemberHonorRequest request
    ) {
        if (!memberRepository.existsByMitgliedsnummer(mitgliedsnummer)) {
            throw new NotFoundException(
                    "Mitglied mit Nummer " + mitgliedsnummer
                            + " wurde nicht gefunden."
            );
        }

        Integer honorTypeId = request.getHonorTypeId();

        if (honorTypeId == null) {
            throw new BadRequestException(
                    "Die Ehrungsart muss angegeben werden."
            );
        }

        if (!lookupRepository.existsHonorTypeById(honorTypeId)) {
            throw new BadRequestException(
                    "Die angegebene Ehrungsart existiert nicht."
            );
        }

        if (memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer,
                honorTypeId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Diese Ehrungsart ist dem Mitglied bereits zugeordnet."
            );
        }

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                request.getHonorDate()
        );

        return memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Die neu angelegte Ehrung konnte nicht gelesen werden."
                ));
    }

    public MemberHonorDto updateHonor(
            String mitgliedsnummer,
            int honorId,
            CreateMemberHonorRequest request
    ) {
        if (!memberRepository.existsByMitgliedsnummer(mitgliedsnummer)) {
            throw new NotFoundException(
                    "Mitglied mit Nummer " + mitgliedsnummer
                            + " wurde nicht gefunden."
            );
        }

        MemberHonorDto existingHonor = memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(honorId, mitgliedsnummer)
                .orElseThrow(() -> new NotFoundException(
                        "Die Ehrung wurde für dieses Mitglied nicht gefunden."
                ));

        Integer honorTypeId = request.getHonorTypeId();

        if (honorTypeId == null) {
            throw new BadRequestException(
                    "Die Ehrungsart muss angegeben werden."
            );
        }

        if (!lookupRepository.existsHonorTypeById(honorTypeId)) {
            throw new BadRequestException(
                    "Die angegebene Ehrungsart existiert nicht."
            );
        }

        if (!honorTypeId.equals(existingHonor.getHonorTypeId())
                && memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer,
                honorTypeId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Diese Ehrungsart ist dem Mitglied bereits zugeordnet."
            );
        }

        int updatedRows = memberHonorRepository.updateHonor(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                request.getHonorDate()
        );

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Die Ehrung konnte nicht aktualisiert werden."
            );
        }

        return memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(honorId, mitgliedsnummer)
                .orElseThrow(() -> new IllegalStateException(
                        "Die aktualisierte Ehrung konnte nicht gelesen werden."
                ));
    }

}
