package de.emc.mitglieder.service.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.exception.BadRequestException;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import de.emc.mitglieder.repository.member.MemberRepository;
import de.emc.mitglieder.repository.LookupRepository;
import de.emc.mitglieder.dto.request.CreateMemberHonorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberHonorServiceTest {

    private MemberHonorRepository memberHonorRepository;
    private MemberRepository memberRepository;
    private MemberHonorService memberHonorService;
    private LookupRepository lookupRepository;

    @BeforeEach
    void setUp() {
        memberHonorRepository = mock(MemberHonorRepository.class);
        memberRepository = mock(MemberRepository.class);
        lookupRepository = mock(LookupRepository.class);

        memberHonorService = new MemberHonorService(
                memberHonorRepository,
                memberRepository,
                lookupRepository
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

    @Test
    void createHonor_shouldCreateHonorForExistingMember() {
        String mitgliedsnummer = "N1234";
        int honorTypeId = 5;
        LocalDate honorDate = LocalDate.of(2026, 11, 17);

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(honorDate);

        MemberHonorDto createdHonor = new MemberHonorDto(
                330,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                honorDate
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(false);

        when(memberHonorRepository.findByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(Optional.of(createdHonor));

        MemberHonorDto result =
                memberHonorService.createHonor(mitgliedsnummer, request);

        assertThat(result).isEqualTo(createdHonor);

        verify(memberHonorRepository).insertHonor(
                mitgliedsnummer,
                honorTypeId,
                honorDate
        );

        verify(memberHonorRepository).findByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer,
                honorTypeId
        );
    }

    @Test
    void createHonor_shouldThrowWhenMemberDoesNotExist() {
        String mitgliedsnummer = "N5000";

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(5);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(false);

        assertThatThrownBy(() ->
                memberHonorService.createHonor(mitgliedsnummer, request)
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(mitgliedsnummer);

        verify(memberRepository)
                .existsByMitgliedsnummer(mitgliedsnummer);

        verifyNoInteractions(
                lookupRepository,
                memberHonorRepository
        );
    }

    @Test
    void createHonor_shouldThrowWhenHonorTypeDoesNotExist() {
        String mitgliedsnummer = "N1234";
        int honorTypeId = 9999;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                memberHonorService.createHonor(mitgliedsnummer, request)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ehrungsart");

        verify(memberRepository)
                .existsByMitgliedsnummer(mitgliedsnummer);

        verify(lookupRepository)
                .existsHonorTypeById(honorTypeId);

        verifyNoInteractions(memberHonorRepository);
    }

    @Test
    void createHonor_shouldThrowWhenHonorTypeAlreadyAssigned() {
        String mitgliedsnummer = "N1234";
        int honorTypeId = 5;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(true);

        assertThatThrownBy(() ->
                memberHonorService.createHonor(mitgliedsnummer, request)
        )
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        ex -> {
                            assertThat(ex.getStatusCode())
                                    .isEqualTo(HttpStatus.CONFLICT);
                            assertThat(ex.getReason())
                                    .contains("bereits zugeordnet");
                        }
                );

        verify(memberHonorRepository)
                .existsByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                );

        verify(memberHonorRepository, never())
                .insertHonor(anyString(), anyInt(), any());
    }

    @Test
    void createHonor_shouldAllowHonorWithoutDate() {
        String mitgliedsnummer = "N1234";
        int honorTypeId = 5;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(null);

        MemberHonorDto createdHonor = new MemberHonorDto(
                331,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                null
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(false);

        when(memberHonorRepository.findByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(Optional.of(createdHonor));

        MemberHonorDto result =
                memberHonorService.createHonor(mitgliedsnummer, request);

        assertThat(result).isEqualTo(createdHonor);

        verify(memberHonorRepository).insertHonor(
                mitgliedsnummer,
                honorTypeId,
                null
        );
    }

    @Test
    void createHonor_shouldAllowFutureHonorDate() {
        String mitgliedsnummer = "N1234";
        int honorTypeId = 5;
        LocalDate futureDate = LocalDate.of(2030, 6, 15);

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(futureDate);

        MemberHonorDto createdHonor = new MemberHonorDto(
                332,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                futureDate
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(false);

        when(memberHonorRepository.findByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, honorTypeId
        )).thenReturn(Optional.of(createdHonor));

        MemberHonorDto result =
                memberHonorService.createHonor(mitgliedsnummer, request);

        assertThat(result).isEqualTo(createdHonor);

        verify(memberHonorRepository).insertHonor(
                mitgliedsnummer,
                honorTypeId,
                futureDate
        );
    }

    @Test
    void updateHonor_shouldUpdateDateWithoutChangingHonorType() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;
        int honorTypeId = 5;
        LocalDate newDate = LocalDate.of(2026, 11, 17);

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(newDate);

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        MemberHonorDto updatedHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                newDate
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        ))
                .thenReturn(Optional.of(existingHonor))
                .thenReturn(Optional.of(updatedHonor));


        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.updateHonor(
                honorId, mitgliedsnummer, honorTypeId, newDate
        )).thenReturn(1);

        MemberHonorDto result = memberHonorService.updateHonor(
                mitgliedsnummer, honorId, request
        );

        assertThat(result).isEqualTo(updatedHonor);

        verify(memberHonorRepository).updateHonor(
                honorId, mitgliedsnummer, honorTypeId, newDate
        );

        verify(memberHonorRepository, never())
                .existsByMitgliedsnummerAndHonorTypeId(
                        anyString(), anyInt()
                );
    }

    @Test
    void updateHonor_shouldRejectDuplicateHonorType() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(6);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                5,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        )).thenReturn(Optional.of(existingHonor));

        when(lookupRepository.existsHonorTypeById(6))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, 6
        )).thenReturn(true);

        assertThatThrownBy(() ->
                memberHonorService.updateHonor(
                        mitgliedsnummer, honorId, request
                )
        ).isInstanceOfSatisfying(
                ResponseStatusException.class,
                ex -> assertThat(ex.getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT)
        );

        verify(memberHonorRepository, never()).updateHonor(
                anyInt(), anyString(), anyInt(), any()
        );
    }

    @Test
    void updateHonor_shouldAllowChangingHonorType() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;
        int newHonorTypeId = 6;
        LocalDate newDate = LocalDate.of(2026, 11, 17);

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(newHonorTypeId);
        request.setHonorDate(newDate);

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                5,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        MemberHonorDto updatedHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                newHonorTypeId,
                "Neue Ehrungsart",
                newDate
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        ))
                .thenReturn(Optional.of(existingHonor))
                .thenReturn(Optional.of(updatedHonor)
        );

        when(lookupRepository.existsHonorTypeById(newHonorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.existsByMitgliedsnummerAndHonorTypeId(
                mitgliedsnummer, newHonorTypeId
        )).thenReturn(false);

        when(memberHonorRepository.updateHonor(
                honorId, mitgliedsnummer, newHonorTypeId, newDate
        )).thenReturn(1);

        MemberHonorDto result = memberHonorService.updateHonor(
                mitgliedsnummer, honorId, request
        );

        assertThat(result).isEqualTo(updatedHonor);

        verify(memberHonorRepository)
                .existsByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer, newHonorTypeId
                );

        verify(memberHonorRepository).updateHonor(
                honorId, mitgliedsnummer, newHonorTypeId, newDate
        );
    }

    @Test
    void updateHonor_shouldThrowWhenMemberDoesNotExist() {
        String mitgliedsnummer = "N5000";
        int honorId = 330;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(5);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(false);

        assertThatThrownBy(() ->
                memberHonorService.updateHonor(
                        mitgliedsnummer, honorId, request
                )
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(mitgliedsnummer);

        verify(memberRepository)
                .existsByMitgliedsnummer(mitgliedsnummer);

        verifyNoInteractions(
                lookupRepository,
                memberHonorRepository
        );
    }

    @Test
    void updateHonor_shouldThrowWhenHonorDoesNotBelongToMember() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(5);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                memberHonorService.updateHonor(
                        mitgliedsnummer, honorId, request
                )
        )
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ehrung");

        verify(memberHonorRepository)
                .findByHonorIdAndMitgliedsnummer(
                        honorId, mitgliedsnummer
                );

        verifyNoInteractions(lookupRepository);

        verify(memberHonorRepository, never()).updateHonor(
                anyInt(), anyString(), anyInt(), any()
        );
    }

    @Test
    void updateHonor_shouldThrowWhenHonorTypeDoesNotExist() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;
        int invalidHonorTypeId = 9999;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(invalidHonorTypeId);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                5,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        )).thenReturn(Optional.of(existingHonor));

        when(lookupRepository.existsHonorTypeById(invalidHonorTypeId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                memberHonorService.updateHonor(
                        mitgliedsnummer, honorId, request
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ehrungsart");

        verify(lookupRepository)
                .existsHonorTypeById(invalidHonorTypeId);

        verify(memberHonorRepository, never()).updateHonor(
                anyInt(), anyString(), anyInt(), any()
        );
    }

    @Test
    void updateHonor_shouldAllowRemovingHonorDate() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;
        int honorTypeId = 5;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(null);

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        MemberHonorDto updatedHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                null
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        ))
                .thenReturn(Optional.of(existingHonor))
                .thenReturn(Optional.of(updatedHonor)
        );

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.updateHonor(
                honorId, mitgliedsnummer, honorTypeId, null
        )).thenReturn(1);

        MemberHonorDto result = memberHonorService.updateHonor(
                mitgliedsnummer, honorId, request
        );

        assertThat(result).isEqualTo(updatedHonor);
        assertThat(result.getHonorDate()).isNull();

        verify(memberHonorRepository).updateHonor(
                honorId, mitgliedsnummer, honorTypeId, null
        );
    }

    @Test
    void updateHonor_shouldAllowFutureHonorDate() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;
        int honorTypeId = 5;
        LocalDate futureDate = LocalDate.of(2030, 6, 15);

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(honorTypeId);
        request.setHonorDate(futureDate);

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        MemberHonorDto updatedHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                "30 Jahre singen im EMC",
                futureDate
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        ))
                .thenReturn(Optional.of(existingHonor))
                .thenReturn(Optional.of(updatedHonor)
        );

        when(lookupRepository.existsHonorTypeById(honorTypeId))
                .thenReturn(true);

        when(memberHonorRepository.updateHonor(
                honorId, mitgliedsnummer, honorTypeId, futureDate
        )).thenReturn(1);

        MemberHonorDto result = memberHonorService.updateHonor(
                mitgliedsnummer, honorId, request
        );

        assertThat(result).isEqualTo(updatedHonor);
        assertThat(result.getHonorDate()).isEqualTo(futureDate);

        verify(memberHonorRepository).updateHonor(
                honorId, mitgliedsnummer, honorTypeId, futureDate
        );
    }

    @Test
    void updateHonor_shouldRejectMissingHonorType() {
        String mitgliedsnummer = "N1234";
        int honorId = 330;

        CreateMemberHonorRequest request = new CreateMemberHonorRequest();
        request.setHonorTypeId(null);
        request.setHonorDate(LocalDate.of(2026, 11, 17));

        MemberHonorDto existingHonor = new MemberHonorDto(
                honorId,
                mitgliedsnummer,
                5,
                "30 Jahre singen im EMC",
                LocalDate.of(2019, 3, 16)
        );

        when(memberRepository.existsByMitgliedsnummer(mitgliedsnummer))
                .thenReturn(true);

        when(memberHonorRepository.findByHonorIdAndMitgliedsnummer(
                honorId, mitgliedsnummer
        )).thenReturn(Optional.of(existingHonor));

        assertThatThrownBy(() ->
                memberHonorService.updateHonor(
                        mitgliedsnummer, honorId, request
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ehrungsart");

        verifyNoInteractions(lookupRepository);

        verify(memberHonorRepository, never()).updateHonor(
                anyInt(), anyString(), anyInt(), any()
        );
    }

}
