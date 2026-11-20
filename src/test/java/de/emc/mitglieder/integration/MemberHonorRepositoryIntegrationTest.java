package de.emc.mitglieder.integration;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberHonorRepositoryIntegrationTest {


    private final MemberHonorRepository memberHonorRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    MemberHonorRepositoryIntegrationTest(
            MemberHonorRepository memberHonorRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.memberHonorRepository = memberHonorRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldReturnHonorsSortedByDateDescendingWithNullLast() {
        String mitgliedsnummer = "N1001";

        insertHonor(mitgliedsnummer, 1, LocalDate.of(2009, 1, 24));
        insertHonor(mitgliedsnummer, 3, LocalDate.of(2014, 5, 10));
        insertHonor(mitgliedsnummer, 5, null);
        insertHonor(mitgliedsnummer, 7, LocalDate.of(2014, 5, 10));

        List<MemberHonorDto> honors =
                memberHonorRepository.findByMitgliedsnummer(mitgliedsnummer);

        assertThat(honors).hasSize(4);

        assertThat(honors)
                .extracting(MemberHonorDto::getHonorTypeId)
                .containsExactly(3, 7, 1, 5);

        assertThat(honors)
                .extracting(MemberHonorDto::getHonorDate)
                .containsExactly(
                        LocalDate.of(2014, 5, 10),
                        LocalDate.of(2014, 5, 10),
                        LocalDate.of(2009, 1, 24),
                        null
                );

        assertThat(honors)
                .extracting(MemberHonorDto::getMitgliedsnummer)
                .containsOnly(mitgliedsnummer);
    }

    @Test
    void shouldReturnEmptyListForMemberWithoutHonors() {
        List<MemberHonorDto> honors =
                memberHonorRepository.findByMitgliedsnummer("N1002");

        assertThat(honors).isEmpty();
    }

    private void insertHonor(
            String mitgliedsnummer,
            int honorTypeId,
            LocalDate honorDate
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO tblEhrungen
                    (Mitgliedsnummer, IDEhrungen, EhrungenTermin)
                VALUES (?, ?, ?)
                """,
                mitgliedsnummer,
                honorTypeId,
                honorDate == null ? null : Date.valueOf(honorDate)
        );
    }

    @Test
    void shouldInsertHonorAndReadItBack() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;
        LocalDate honorDate = LocalDate.of(2026, 11, 17);

        assertThat(memberHonorRepository
                .existsByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )).isFalse();

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                honorDate
        );

        assertThat(memberHonorRepository
                .existsByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )).isTrue();

        MemberHonorDto honor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        assertThat(honor.getHonorId()).isPositive();
        assertThat(honor.getMitgliedsnummer())
                .isEqualTo(mitgliedsnummer);
        assertThat(honor.getHonorTypeId())
                .isEqualTo(honorTypeId);
        assertThat(honor.getHonorName())
                .isNotBlank();
        assertThat(honor.getHonorDate())
                .isEqualTo(honorDate);
    }

    @Test
    void shouldInsertHonorWithoutDate() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                null
        );

        MemberHonorDto honor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        assertThat(honor.getHonorId()).isPositive();
        assertThat(honor.getHonorTypeId())
                .isEqualTo(honorTypeId);
        assertThat(honor.getHonorDate()).isNull();
    }

    @Test
    void shouldRejectDuplicateHonorTypeForSameMember() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                LocalDate.of(2020, 5, 10)
        );

        assertThatThrownBy(() ->
                memberHonorRepository.insertHonor(
                        mitgliedsnummer,
                        honorTypeId,
                        LocalDate.of(2026, 11, 17)
                )
        )
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldUpdateHonorDateAndReadItBack() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;

        LocalDate originalDate = LocalDate.of(2020, 5, 10);
        LocalDate updatedDate = LocalDate.of(2026, 11, 17);

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                originalDate
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int updatedRows = memberHonorRepository.updateHonor(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                updatedDate
        );

        assertThat(updatedRows).isEqualTo(1);

        MemberHonorDto updatedHonor = memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        mitgliedsnummer
                )
                .orElseThrow();

        assertThat(updatedHonor.getHonorId())
                .isEqualTo(honorId);

        assertThat(updatedHonor.getHonorTypeId())
                .isEqualTo(honorTypeId);

        assertThat(updatedHonor.getHonorDate())
                .isEqualTo(updatedDate);
    }

    @Test
    void shouldRemoveHonorDateAndReadBackNull() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                LocalDate.of(2020, 5, 10)
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int updatedRows = memberHonorRepository.updateHonor(
                honorId,
                mitgliedsnummer,
                honorTypeId,
                null
        );

        assertThat(updatedRows).isEqualTo(1);

        MemberHonorDto updatedHonor = memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        mitgliedsnummer
                )
                .orElseThrow();

        assertThat(updatedHonor.getHonorDate()).isNull();
        assertThat(updatedHonor.getHonorTypeId())
                .isEqualTo(honorTypeId);
    }

    @Test
    void shouldNotUpdateHonorBelongingToAnotherMember() {
        String ownerMitgliedsnummer = "N1001";
        String otherMitgliedsnummer = "N1002";
        int honorTypeId = 5;

        LocalDate originalDate = LocalDate.of(2020, 5, 10);
        LocalDate attemptedDate = LocalDate.of(2026, 11, 17);

        memberHonorRepository.insertHonor(
                ownerMitgliedsnummer,
                honorTypeId,
                originalDate
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        ownerMitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int updatedRows = memberHonorRepository.updateHonor(
                honorId,
                otherMitgliedsnummer,
                honorTypeId,
                attemptedDate
        );

        assertThat(updatedRows).isZero();

        assertThat(memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        otherMitgliedsnummer
                )
        ).isEmpty();

        MemberHonorDto unchangedHonor = memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        ownerMitgliedsnummer
                )
                .orElseThrow();

        assertThat(unchangedHonor.getHonorDate())
                .isEqualTo(originalDate);

        assertThat(unchangedHonor.getHonorTypeId())
                .isEqualTo(honorTypeId);
    }

    @Test
    void shouldUpdateHonorTypeAndReadItBack() {
        String mitgliedsnummer = "N1001";
        int originalHonorTypeId = 5;
        int newHonorTypeId = 6;

        LocalDate originalDate = LocalDate.of(2020, 5, 10);
        LocalDate updatedDate = LocalDate.of(2026, 11, 17);

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                originalHonorTypeId,
                originalDate
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        originalHonorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int updatedRows = memberHonorRepository.updateHonor(
                honorId,
                mitgliedsnummer,
                newHonorTypeId,
                updatedDate
        );

        assertThat(updatedRows).isEqualTo(1);

        MemberHonorDto updatedHonor = memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        mitgliedsnummer
                )
                .orElseThrow();

        assertThat(updatedHonor.getHonorId())
                .isEqualTo(honorId);

        assertThat(updatedHonor.getHonorTypeId())
                .isEqualTo(newHonorTypeId);

        assertThat(updatedHonor.getHonorDate())
                .isEqualTo(updatedDate);

        assertThat(memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        originalHonorTypeId
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectDuplicateHonorTypeOnUpdate() {
        String mitgliedsnummer = "N1001";
        int originalHonorTypeId = 5;
        int duplicateHonorTypeId = 6;

        LocalDate originalDate = LocalDate.of(2020, 5, 10);

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                originalHonorTypeId,
                originalDate
        );

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                duplicateHonorTypeId,
                LocalDate.of(2022, 6, 15)
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        originalHonorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        assertThatThrownBy(() ->
                memberHonorRepository.updateHonor(
                        honorId,
                        mitgliedsnummer,
                        duplicateHonorTypeId,
                        LocalDate.of(2026, 11, 17)
                )
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldDeleteHonorAndVerifyItIsGone() {
        String mitgliedsnummer = "N1001";
        int honorTypeId = 5;

        memberHonorRepository.insertHonor(
                mitgliedsnummer,
                honorTypeId,
                LocalDate.of(2020, 5, 10)
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        mitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int deletedRows = memberHonorRepository.deleteHonor(
                honorId,
                mitgliedsnummer
        );

        assertThat(deletedRows).isEqualTo(1);

        assertThat(memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        mitgliedsnummer
                )
        ).isEmpty();
    }

    @Test
    void shouldNotDeleteHonorBelongingToAnotherMember() {
        String ownerMitgliedsnummer = "N1001";
        String otherMitgliedsnummer = "N1002";
        int honorTypeId = 5;

        memberHonorRepository.insertHonor(
                ownerMitgliedsnummer,
                honorTypeId,
                LocalDate.of(2020, 5, 10)
        );

        MemberHonorDto existingHonor = memberHonorRepository
                .findByMitgliedsnummerAndHonorTypeId(
                        ownerMitgliedsnummer,
                        honorTypeId
                )
                .orElseThrow();

        int honorId = existingHonor.getHonorId();

        int deletedRows = memberHonorRepository.deleteHonor(
                honorId,
                otherMitgliedsnummer
        );

        assertThat(deletedRows).isZero();

        assertThat(memberHonorRepository
                .findByHonorIdAndMitgliedsnummer(
                        honorId,
                        ownerMitgliedsnummer
                )
        ).isPresent();
    }

}
