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
                SELECT 
                    IDMitgliederstatus,
                    Mitgliederstatus
                FROM tblMitgliederstatus_FT
                ORDER BY IDMitgliederstatus
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new LookupItemDto(
                        rs.getInt("IDMitgliederstatus"),
                        rs.getString("Mitgliederstatus")
                )
        );
    }

    public List<LookupItemDto> findVoices() {
        String sql = """
            SELECT 
                IDStimme,
                Stimme
            FROM tblStimme_FT
            ORDER BY IDStimme
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new LookupItemDto(
                        rs.getInt("IDStimme"),
                        rs.getString("Stimme")
                )
        );
    }
}