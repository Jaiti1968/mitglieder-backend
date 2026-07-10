package de.emc.mitglieder.reporting.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BirthdayReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public BirthdayReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BirthdayReportMemberData> findBirthdayReportMembers(
            List<Integer> mitgliedsstatusIds,
            List<Integer> stimmeIds
    ) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.Mitgliedsnummer,
                    m.Vorname,
                    m.Nachname,
                    m.Geburtsdatum,

                    ms.IDMitgliederstatus,
                    msf.Mitgliederstatus,

                    ms.IDStimme,
                    sf.Stimme

                FROM tblMitglieder m

                LEFT JOIN tblMitgliedschaft ms
                    ON ms.Mitgliedsnummer = m.Mitgliedsnummer

                LEFT JOIN tblMitgliederstatus_FT msf
                    ON msf.IDMitgliederstatus = ms.IDMitgliederstatus

                LEFT JOIN tblStimme_FT sf
                    ON sf.IDStimme = ms.IDStimme

                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (mitgliedsstatusIds != null && !mitgliedsstatusIds.isEmpty()) {

            sql.append("""
                    
                    AND ms.IDMitgliederstatus IN (
                    """);

            sql.append(
                    String.join(
                            ",",
                            java.util.Collections.nCopies(
                                    mitgliedsstatusIds.size(),
                                    "?"
                            )
                    )
            );

            sql.append(")");

            params.addAll(mitgliedsstatusIds);
        }

        if (stimmeIds != null && !stimmeIds.isEmpty()) {

            sql.append("""
                    
                    AND ms.IDStimme IN (
                    """);

            sql.append(
                    String.join(
                            ",",
                            java.util.Collections.nCopies(
                                    stimmeIds.size(),
                                    "?"
                            )
                    )
            );

            sql.append(")");

            params.addAll(stimmeIds);
        }

        sql.append("""
                
                ORDER BY
                    MONTH(m.Geburtsdatum),
                    DAY(m.Geburtsdatum),
                    m.Nachname,
                    m.Vorname
                """);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new BirthdayReportMemberData(
                        rs.getString("Mitgliedsnummer"),
                        rs.getString("Vorname"),
                        rs.getString("Nachname"),
                        (Integer) rs.getObject("IDMitgliederstatus"),
                        rs.getString("Mitgliederstatus"),
                        (Integer) rs.getObject("IDStimme"),
                        rs.getString("Stimme"),
                        rs.getDate("Geburtsdatum") != null
                                ? rs.getDate("Geburtsdatum").toLocalDate()
                                : null
                ),
                params.toArray()
        );
    }
}