package de.emc.mitglieder.repository.member;

import de.emc.mitglieder.dto.member.MemberHonorDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

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

    public boolean existsByMitgliedsnummerAndHonorTypeId(
            String mitgliedsnummer,
            int honorTypeId
    ) {
        String sql = """
            SELECT COUNT(*)
            FROM tblEhrungen
            WHERE Mitgliedsnummer = ?
              AND IDEhrungen = ?
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                mitgliedsnummer,
                honorTypeId
        );

        return count != null && count > 0;
    }

    public void insertHonor(
            String mitgliedsnummer,
            int honorTypeId,
            LocalDate honorDate
    ) {
        String sql = """
            INSERT INTO tblEhrungen (
                Mitgliedsnummer,
                IDEhrungen,
                EhrungenTermin
            )
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                mitgliedsnummer,
                honorTypeId,
                honorDate
        );
    }

    public Optional<MemberHonorDto> findByMitgliedsnummerAndHonorTypeId(
            String mitgliedsnummer,
            int honorTypeId
    ) {
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
              AND e.IDEhrungen = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MemberHonorDto(
                        rs.getInt("IDTabEhrungen"),
                        rs.getString("Mitgliedsnummer"),
                        rs.getInt("IDEhrungen"),
                        rs.getString("Ehrungen"),
                        rs.getObject(
                                "EhrungenTermin",
                                java.time.LocalDate.class
                        )
                ),
                mitgliedsnummer,
                honorTypeId
        ).stream().findFirst();
    }

    public Optional<MemberHonorDto> findByHonorIdAndMitgliedsnummer(
            int honorId,
            String mitgliedsnummer
    ) {
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
            WHERE e.IDTabEhrungen = ?
              AND e.Mitgliedsnummer = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MemberHonorDto(
                        rs.getInt("IDTabEhrungen"),
                        rs.getString("Mitgliedsnummer"),
                        rs.getInt("IDEhrungen"),
                        rs.getString("Ehrungen"),
                        rs.getObject(
                                "EhrungenTermin",
                                LocalDate.class
                        )
                ),
                honorId,
                mitgliedsnummer
        ).stream().findFirst();
    }

    public int updateHonor(
            int honorId,
            String mitgliedsnummer,
            int honorTypeId,
            LocalDate honorDate
    ) {
        String sql = """
            UPDATE tblEhrungen
            SET IDEhrungen = ?,
                EhrungenTermin = ?
            WHERE IDTabEhrungen = ?
              AND Mitgliedsnummer = ?
            """;

        return jdbcTemplate.update(
                sql,
                honorTypeId,
                honorDate,
                honorId,
                mitgliedsnummer
        );
    }

    public int deleteHonor(int honorId, String mitgliedsnummer) {
        String sql = """
            DELETE FROM tblEhrungen
            WHERE IDTabEhrungen = ?
              AND Mitgliedsnummer = ?
            """;

        return jdbcTemplate.update(
                sql,
                honorId,
                mitgliedsnummer
        );
    }
}
