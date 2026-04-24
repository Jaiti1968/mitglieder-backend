package de.emc.mitglieder.repository.member;

import de.emc.mitglieder.dto.member.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MemberListItemDto> findMembers(
            String search,
            Integer statusId,
            Integer stimmeId,
            int page,
            int pageSize
    ) {
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.Mitgliedsnummer,
                    m.Vorname,
                    m.Nachname,
                    m.Ort,
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
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                    
                    AND (
                        m.Mitgliedsnummer LIKE ?
                        OR m.Vorname LIKE ?
                        OR m.Nachname LIKE ?
                        OR m.Ort LIKE ?
                    )
                    """);
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (statusId != null) {
            sql.append("\n AND ms.IDMitgliederstatus = ?");
            params.add(statusId);
        }

        if (stimmeId != null) {
            sql.append("\n AND ms.IDStimme = ?");
            params.add(stimmeId);
        }

        sql.append("""
                
                ORDER BY m.Nachname, m.Vorname
                LIMIT ? OFFSET ?
                """);

        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
                        new MemberListItemDto(
                                rs.getString("Mitgliedsnummer"),
                                rs.getString("Vorname"),
                                rs.getString("Nachname"),
                                rs.getString("Ort"),
                                (Integer) rs.getObject("IDMitgliederstatus"),
                                rs.getString("Mitgliederstatus"),
                                (Integer) rs.getObject("IDStimme"),
                                rs.getString("Stimme")
                        ),
                params.toArray()
        );
    }

    public long countMembers(
            String search,
            Integer statusId,
            Integer stimmeId
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM tblMitglieder m
                LEFT JOIN tblMitgliedschaft ms
                    ON ms.Mitgliedsnummer = m.Mitgliedsnummer
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                    
                    AND (
                        m.Mitgliedsnummer LIKE ?
                        OR m.Vorname LIKE ?
                        OR m.Nachname LIKE ?
                        OR m.Ort LIKE ?
                    )
                    """);
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (statusId != null) {
            sql.append("\n AND ms.IDMitgliederstatus = ?");
            params.add(statusId);
        }

        if (stimmeId != null) {
            sql.append("\n AND ms.IDStimme = ?");
            params.add(stimmeId);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }

    public MemberDetailDto findMemberById(String mitgliedsnummer) {
        String sql = """
                SELECT
                    m.Mitgliedsnummer,
                    m.Vorname,
                    m.Nachname,
                    m.Ort,

                    k.Telefon_privat,
                    k.Telefon_geschaeftlich,
                    k.Mobiltelefon,
                    k.EMail,
                    k.Adresszusatz,
                    k.Briefanrede,

                    ms.IDMitgliederstatus,
                    msf.Mitgliederstatus,
                    ms.IDStimme,
                    sf.Stimme

                FROM tblMitglieder m

                LEFT JOIN tblKontaktdaten k
                    ON k.Mitgliedsnummer = m.Mitgliedsnummer

                LEFT JOIN tblMitgliedschaft ms
                    ON ms.Mitgliedsnummer = m.Mitgliedsnummer

                LEFT JOIN tblMitgliederstatus_FT msf
                    ON msf.IDMitgliederstatus = ms.IDMitgliederstatus

                LEFT JOIN tblStimme_FT sf
                    ON sf.IDStimme = ms.IDStimme

                WHERE m.Mitgliedsnummer = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new MemberDetailDto(
                                rs.getString("Mitgliedsnummer"),

                                new StammdatenDto(
                                        rs.getString("Vorname"),
                                        rs.getString("Nachname"),
                                        rs.getString("Ort")
                                ),

                                new KontaktDto(
                                        rs.getString("Telefon_privat"),
                                        rs.getString("Telefon_geschaeftlich"),
                                        rs.getString("Mobiltelefon"),
                                        rs.getString("EMail"),
                                        rs.getString("Adresszusatz"),
                                        rs.getString("Briefanrede")
                                ),

                                new MitgliedschaftDto(
                                        (Integer) rs.getObject("IDMitgliederstatus"),
                                        rs.getString("Mitgliederstatus"),
                                        (Integer) rs.getObject("IDStimme"),
                                        rs.getString("Stimme")
                                )
                        ),
                mitgliedsnummer
        );
    }

    public void updateStammdaten(
            String mitgliedsnummer,
            String vorname,
            String nachname,
            String ort
    ) {
        String sql = """
            UPDATE tblMitglieder
            SET Vorname = ?, Nachname = ?, Ort = ?
            WHERE Mitgliedsnummer = ?
            """;

        jdbcTemplate.update(sql, vorname, nachname, ort, mitgliedsnummer);
    }

    public void updateKontakt(
            String mitgliedsnummer,
            String telefonPrivat,
            String telefonGeschaeftlich,
            String mobiltelefon,
            String email,
            String adresszusatz,
            String briefanrede
    ) {
        String sql = """
            UPDATE tblKontaktdaten
            SET
                Telefon_privat = ?,
                Telefon_geschaeftlich = ?,
                Mobiltelefon = ?,
                EMail = ?,
                Adresszusatz = ?,
                Briefanrede = ?
            WHERE Mitgliedsnummer = ?
            """;

        jdbcTemplate.update(
                sql,
                telefonPrivat,
                telefonGeschaeftlich,
                mobiltelefon,
                email,
                adresszusatz,
                briefanrede,
                mitgliedsnummer
        );
    }

    public void updateMitgliedschaft(
            String mitgliedsnummer,
            Integer statusId,
            Integer stimmeId
    ) {
        String sql = """
            UPDATE tblMitgliedschaft
            SET
                IDMitgliederstatus = ?,
                IDStimme = ?
            WHERE Mitgliedsnummer = ?
            """;

        jdbcTemplate.update(sql, statusId, stimmeId, mitgliedsnummer);
    }
}