
package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberHonorServiceTest {

    private MemberHonorRepository memberHonorRepository;
    private MemberRepository memberRepository;
    private MemberHonorService memberHonorService;

    @BeforeEach
    void setUp() {
        memberHonorRepository = mock(MemberHonorRepository.class);
        memberRepository = mock(MemberRepository.class);

        memberHonorService = new MemberHonorService(
                memberHonorRepository,
                memberRepository
        );
    }

    @Test
    void getHonors_shouldReturnHonorsForExistingMember() {
        String mitgliedsnummer = "N1234";

        List<MemberHonorDto> honors = List.of(
                new MemberHonorDto(
                        291,
                        mitgliedsnummer,
                        5,
                        "30 Jahre singen im EMC",
                        LocalDate.of(2019, 3, 16)
                )
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(honors);

        List<MemberHonorDto> result =
                memberHonorService.getHonors(mitgliedsnummer);

        assertThat(result).containsExactlyElementsOf(honors);

        verify(memberRepository)
                .existsByMitgliedsnummer(mitgliedsnummer);

        verify(memberHonorRepository)
                .findByMitgliedsnummer(mitgliedsnummer);
    }

    @Test
    void getHonors_shouldReturnEmptyListForMemberWithoutHonors() {
        String mitgliedsnummer = "N1234";

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(List.of());

        List<MemberHonorDto> result =
                memberHonorService.getHonors(mitgliedsnummer);

        assertThat(result).isEmpty();

        verify(memberHonorRepository)
                .findByMitgliedsnummer(mitgliedsnummer);
    }

    @Test
    void getHonors_shouldThrowWhenMemberDoesNotExist() {
        String mitgliedsnummer = "N5000";

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(false);

        assertThatThrownBy(() ->
                memberHonorService.getHonors(mitgliedsnummer)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(mitgliedsnummer);

        verify(memberHonorRepository, never())
                .findByMitgliedsnummer(anyString());
    }
}
