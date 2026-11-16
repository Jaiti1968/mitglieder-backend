
package de.emc.mitglieder.integration;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import de.emc.mitglieder.repository.member.MemberHonorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberHonorRepositoryIntegrationTest {

    @Autowired
    private MemberHonorRepository memberHonorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
}
