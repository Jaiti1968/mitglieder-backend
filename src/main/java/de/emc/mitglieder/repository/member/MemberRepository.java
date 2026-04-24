package de.emc.mitglieder.repository.member;

import de.emc.mitglieder.dto.member.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import de.emc.mitglieder.exception.NotFoundException;

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

        try {
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
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
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

        int updatedRows = jdbcTemplate.update(sql, vorname, nachname, ort, mitgliedsnummer);

        if (updatedRows == 0) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
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

        int updatedRows = jdbcTemplate.update(
                sql,
                telefonPrivat,
                telefonGeschaeftlich,
                mobiltelefon,
                email,
                adresszusatz,
                briefanrede,
                mitgliedsnummer
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
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

        int updatedRows = jdbcTemplate.update(sql, statusId, stimmeId, mitgliedsnummer);

        if (updatedRows == 0) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
    }

    public String getCurrentMitgliedsnummer() {
        String sql = "SELECT neueMitgliedsnummer FROM tblAllgemein_FT";
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    public String getNextMitgliedsnummer(String current) {
        int number = Integer.parseInt(current.substring(1));
        return "N" + (number + 1);
    }

    public void updateNeueMitgliedsnummer(String nextNumber) {
        String sql = "UPDATE tblAllgemein_FT SET neueMitgliedsnummer = ?";
        jdbcTemplate.update(sql, nextNumber);
    }

    public void insertMitglied(String id, StammdatenDto s) {
        String sql = """
        INSERT INTO tblMitglieder (
            Mitgliedsnummer,
            Anrede,
            AkademischerTitel,
            Vorname,
            Nachname,
            Ort
        )
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(
                sql,
                id,
                "Herr",
                "",
                s != null ? s.vorname() : null,
                s != null ? s.nachname() : null,
                s != null ? s.ort() : null
        );
    }

    public void insertMitgliedschaft(String id, MitgliedschaftDto m) {
        String sql = """
        INSERT INTO tblMitgliedschaft (
            Mitgliedsnummer,
            IDMitgliederstatus,
            IDStimme,
            Kammerchor
        )
        VALUES (?, ?, ?, ?)
    """;

        jdbcTemplate.update(
                sql,
                id,
                m != null && m.mitgliedsstatusId() != null ? m.mitgliedsstatusId() : 4,
                m != null && m.stimmeId() != null ? m.stimmeId() : 6,
                0 // Default wie Access
        );
    }

    public void insertKontakt(String id, KontaktDto k) {
        String sql = """
        INSERT INTO tblKontaktdaten (
            Mitgliedsnummer,
            Telefon_privat,
            Telefon_geschaeftlich,
            Mobiltelefon,
            EMail,
            Adresszusatz,
            Briefanrede
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(
                sql,
                id,
                k != null ? k.telefonPrivat() : null,
                k != null ? k.telefonGeschaeftlich() : null,
                k != null ? k.mobiltelefon() : null,
                k != null ? k.email() : null,
                k != null ? k.adresszusatz() : null,
                k != null && k.briefanrede() != null ? k.briefanrede() : "Lieber Sangesfreund"
        );
    }

    public void insertChorkleidung(String id) {
        String sql = """
        INSERT INTO tblChorkleidung (
            Mitgliedsnummer,
            Neubeschaffung,
            Barzahlung
        )
        VALUES (?, 0, 0)
    """;

        jdbcTemplate.update(sql, id);
    }

    public void insertDatenschutz(String id) {
        String sql = """
        INSERT INTO tblDatenschutz (
            Mitgliedsnummer
        )
        VALUES (?)
    """;

        jdbcTemplate.update(sql, id);
    }
}