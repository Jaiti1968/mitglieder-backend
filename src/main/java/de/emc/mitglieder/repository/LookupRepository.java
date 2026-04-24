package de.emc.mitglieder.repository;

import de.emc.mitglieder.dto.LookupItemDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LookupRepository {

    private final JdbcTemplate jdbcTemplate;

    public LookupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LookupItemDto> findMemberStatus() {
        String sql = """
                 SELECT\s
                     IDMitgliederstatus,
                     Mitgliederstatus
                 FROM tblMitgliederstatus_FT
                 ORDER BY IDMitgliederstatus
                \s""";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new LookupItemDto(
                        rs.getInt("IDMitgliederstatus"),
                        rs.getString("Mitgliederstatus")
                )
        );
    }

    public List<LookupItemDto> findVoices() {
        String sql = """
                 SELECT\s
                     IDStimme,
                     Stimme
                 FROM tblStimme_FT
                 ORDER BY IDStimme
                \s""";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new LookupItemDto(
                        rs.getInt("IDStimme"),
                        rs.getString("Stimme")
                )
        );
    }

    public boolean existsMemberStatus(Integer id) {
        String sql = """
                SELECT COUNT(*)
                FROM tblMitgliederstatus_FT
                WHERE IDMitgliederstatus = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsVoice(Integer id) {
        String sql = """
                SELECT COUNT(*)
                FROM tblStimme_FT
                WHERE IDStimme = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}