package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.request.UpdateChorkleidungRequest;
import de.emc.mitglieder.dto.request.UpdateDatenschutzRequest;
import de.emc.mitglieder.dto.request.UpdateMitgliedschaftRequest;
import de.emc.mitglieder.exception.BusinessValidationException;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.repository.LookupRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    private MemberRepository memberRepository;
    private LookupRepository lookupRepository;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        lookupRepository = mock(LookupRepository.class);
        memberService = new MemberService(memberRepository, lookupRepository);
    }

    @Test
    void updateMitgliedschaft_shouldThrowWhenAustrittBeforeEintritt() {
        UpdateMitgliedschaftRequest request = new UpdateMitgliedschaftRequest();
        request.setEintritt(LocalDate.of(2025, 5, 10));
        request.setAustritt(LocalDate.of(2025, 5, 1));
        request.setMitgliedsstatusId(1);
        request.setStimmeId(1);

        assertThatThrownBy(() ->
                memberService.updateMitgliedschaft("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateMitgliedschaft(anyString(), any());
    }

    @Test
    void updateMitgliedschaft_shouldThrowForInvalidMemberStatus() {
        UpdateMitgliedschaftRequest request = new UpdateMitgliedschaftRequest();
        request.setMitgliedsstatusId(999);
        request.setStimmeId(1);

        when(lookupRepository.existsMemberStatus(999)).thenReturn(false);

        assertThatThrownBy(() ->
                memberService.updateMitgliedschaft("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateMitgliedschaft(anyString(), any());
    }

    @Test
    void updateMitgliedschaft_shouldThrowForInvalidVoice() {
        UpdateMitgliedschaftRequest request = new UpdateMitgliedschaftRequest();
        request.setMitgliedsstatusId(1);
        request.setStimmeId(999);

        when(lookupRepository.existsMemberStatus(1)).thenReturn(true);
        when(lookupRepository.existsVoice(999)).thenReturn(false);

        assertThatThrownBy(() ->
                memberService.updateMitgliedschaft("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateMitgliedschaft(anyString(), any());
    }

    @Test
    void updateDatenschutz_shouldThrowForFutureDate() {
        UpdateDatenschutzRequest request = new UpdateDatenschutzRequest();
        request.setDatumDatenschutz(LocalDate.now().plusDays(1));

        assertThatThrownBy(() ->
                memberService.updateDatenschutz("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateDatenschutz(anyString(), any());
    }

    @Test
    void updateChorkleidung_shouldThrowWhenRueckgabeBeforeUebergabe() {
        UpdateChorkleidungRequest request = new UpdateChorkleidungRequest();
        request.setUebergabeAm(LocalDate.of(2025, 5, 10));
        request.setRueckgabeAm(LocalDate.of(2025, 5, 1));

        assertThatThrownBy(() ->
                memberService.updateChorkleidung("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateChorkleidung(anyString(), any());
    }

    @Test
    void updateChorkleidung_shouldThrowWhenSommerRueckgabeBeforeErhalten() {
        UpdateChorkleidungRequest request = new UpdateChorkleidungRequest();
        request.setSommerkleidungErhalten(LocalDate.of(2025, 6, 10));
        request.setSommerkleidungRueckgabe(LocalDate.of(2025, 6, 1));

        assertThatThrownBy(() ->
                memberService.updateChorkleidung("N1234", request)
        ).isInstanceOf(BusinessValidationException.class);

        verify(memberRepository, never()).updateChorkleidung(anyString(), any());
    }

    @Test
    void getDatenschutz_shouldThrowWhenNotFound() {
        when(memberRepository.findDatenschutzByMitgliedsnummer("N1234"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                memberService.getDatenschutz("N1234")
        ).isInstanceOf(NotFoundException.class);

        verify(memberRepository).findDatenschutzByMitgliedsnummer("N1234");
    }

    @Test
    void getChorkleidung_shouldThrowWhenNotFound() {
        when(memberRepository.findChorkleidungByMitgliedsnummer("N1234"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                memberService.getChorkleidung("N1234")
        ).isInstanceOf(NotFoundException.class);

        verify(memberRepository).findChorkleidungByMitgliedsnummer("N1234");
    }
}