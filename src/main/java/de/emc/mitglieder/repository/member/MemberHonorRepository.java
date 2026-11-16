
package de.emc.mitglieder.repository.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberHonorRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberHonorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MemberHonorDto> findByMitgliedsnummer(String mitgliedsnummer) {
        String sql = """
                SELECT
                    e.IDTabEhrungen,
                    e.Mitgliedsnummer,
                    e.IDEhrungen,
                    ft.Ehrungen,
                    e.EhrungenTermin
                FROM tblEhrungen e
                INNER JOIN tblEhrungen_FT ft
                    ON ft.IDEhrungen = e.IDEhrungen
                WHERE e.Mitgliedsnummer = ?
                ORDER BY
                    CASE WHEN e.EhrungenTermin IS NULL THEN 1 ELSE 0 END,
                    e.EhrungenTermin DESC,
                    e.IDEhrungen ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new MemberHonorDto(
                                rs.getInt("IDTabEhrungen"),
                                rs.getString("Mitgliedsnummer"),
                                rs.getInt("IDEhrungen"),
                                rs.getString("Ehrungen"),
                                rs.getObject("EhrungenTermin", java.time.LocalDate.class)
                        ),
                mitgliedsnummer
        );
    }
}
