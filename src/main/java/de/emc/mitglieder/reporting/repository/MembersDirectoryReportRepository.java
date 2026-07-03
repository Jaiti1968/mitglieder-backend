package de.emc.mitglieder.reporting.repository;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportMemberDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MembersDirectoryReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public MembersDirectoryReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MembersDirectoryReportMemberDto> findMembersDirectory(
            String search,
            List<Integer> mitgliedsstatusIds,
            List<Integer> stimmeIds
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.Mitgliedsnummer,
                    m.Vorname,
                    m.Nachname,
                    m.PLZ,
                    m.Ort,
                    m.StrasseHausNr,
                    m.Geburtsdatum,

                    k.Telefon_privat,
                    k.Telefon_geschaeftlich,
                    k.Mobiltelefon,
                    k.EMail,

                    ms.IDMitgliederstatus,
                    msf.Mitgliederstatus,
                    ms.IDStimme,
                    sf.Stimme,
                    ms.Kammerchor

                FROM tblMitglieder m

                LEFT JOIN tblKontaktdaten k
                    ON k.Mitgliedsnummer = m.Mitgliedsnummer

                LEFT JOIN tblMitgliedschaft ms
                    ON ms.Mitgliedsnummer = m.Mitgliedsnummer

                LEFT JOIN tblMitgliederstatus_FT msf
                    ON msf.IDMitgliederstatus = ms.IDMitgliederstatus

                LEFT JOIN tblStimme_FT sf
                    ON sf.IDStimme = ms.IDStimme

                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                    
                    AND (
                        m.Mitgliedsnummer LIKE ?
                        OR m.Vorname LIKE ?
                        OR m.Nachname LIKE ?
                        OR m.PLZ LIKE ?
                        OR m.Ort LIKE ?
                        OR m.StrasseHausNr LIKE ?
                        OR k.Telefon_privat LIKE ?
                        OR k.Telefon_geschaeftlich LIKE ?
                        OR k.Mobiltelefon LIKE ?
                        OR k.EMail LIKE ?
                    )
                    """);

            String like = "%" + search.trim() + "%";

            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (mitgliedsstatusIds != null && !mitgliedsstatusIds.isEmpty()) {
            sql.append("\n AND ms.IDMitgliederstatus IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(mitgliedsstatusIds.size(), "?")));
            sql.append(")");
            params.addAll(mitgliedsstatusIds);
        }

        if (stimmeIds != null && !stimmeIds.isEmpty()) {
            sql.append("\n AND ms.IDStimme IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(stimmeIds.size(), "?")));
            sql.append(")");
            params.addAll(stimmeIds);
        }

        sql.append("""
                
                ORDER BY
                    ms.IDMitgliederstatus,
                    ms.IDStimme,
                    m.Nachname,
                    m.Vorname
                """);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new MembersDirectoryReportMemberDto(
                        rs.getString("Mitgliedsnummer"),
                        rs.getString("Vorname"),
                        rs.getString("Nachname"),

                        rs.getString("PLZ"),
                        rs.getString("Ort"),
                        rs.getString("StrasseHausNr"),
                        rs.getDate("Geburtsdatum") != null
                                ? rs.getDate("Geburtsdatum").toLocalDate()
                                : null,

                        rs.getString("Telefon_privat"),
                        rs.getString("Telefon_geschaeftlich"),
                        rs.getString("Mobiltelefon"),
                        rs.getString("EMail"),

                        (Integer) rs.getObject("IDMitgliederstatus"),
                        rs.getString("Mitgliederstatus"),
                        (Integer) rs.getObject("IDStimme"),
                        rs.getString("Stimme"),
                        (Boolean) rs.getObject("Kammerchor")
                ),
                params.toArray()
        );
    }
}