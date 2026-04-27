package de.emc.mitglieder.repository.member;

import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.dto.request.UpdateMitgliedschaftRequest;
import de.emc.mitglieder.dto.request.UpdateStammdatenRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.constant.MemberDefaults;
import de.emc.mitglieder.mapper.MemberMapper;

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
            List<Integer> statusIds,
            List<Integer> stimmeIds,
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

        if (statusIds != null && !statusIds.isEmpty()) {
            sql.append("\n AND ms.IDMitgliederstatus IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(statusIds.size(), "?")));
            sql.append(")");
            params.addAll(statusIds);
        }

        if (stimmeIds != null && !stimmeIds.isEmpty()) {
            sql.append("\n AND ms.IDStimme IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(stimmeIds.size(), "?")));
            sql.append(")");
            params.addAll(stimmeIds);
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
            List<Integer> statusIds,
            List<Integer> stimmeIds
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

        if (statusIds != null && !statusIds.isEmpty()) {
            sql.append("\n AND ms.IDMitgliederstatus IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(statusIds.size(), "?")));
            sql.append(")");
            params.addAll(statusIds);
        }

        if (stimmeIds != null && !stimmeIds.isEmpty()) {
            sql.append("\n AND ms.IDStimme IN (");
            sql.append(String.join(",", java.util.Collections.nCopies(stimmeIds.size(), "?")));
            sql.append(")");
            params.addAll(stimmeIds);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }

    public MemberDetailDto findMemberById(String mitgliedsnummer) {
        String sql = """
                SELECT
                    m.Mitgliedsnummer,
                    m.Anrede,
                    m.AkademischerTitel,
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
                    k.Adresszusatz,
                    k.Briefanrede,

                    ms.Eintritt,
                    ms.Austritt,
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

                WHERE m.Mitgliedsnummer = ?
                """;

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> MemberMapper.mapMemberDetail(rs),
                    mitgliedsnummer
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
    }

    public void updateStammdaten(
            String mitgliedsnummer,
            UpdateStammdatenRequest r
    ) {
        String sql = """
            UPDATE tblMitglieder
            SET
                Anrede = ?,
                AkademischerTitel = ?,
                Vorname = ?,
                Nachname = ?,
                PLZ = ?,
                Ort = ?,
                StrasseHausNr = ?,
                Geburtsdatum = ?
            WHERE Mitgliedsnummer = ?
            """;

        int updatedRows = jdbcTemplate.update(
                sql,
                r.anrede(),
                r.akademischerTitel(),
                r.vorname(),
                r.nachname(),
                r.plz(),
                r.ort(),
                r.strasseHausNr(),
                r.geburtsdatum(),
                mitgliedsnummer
        );

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
            UpdateMitgliedschaftRequest r
    ) {
        String sql = """
            UPDATE tblMitgliedschaft
            SET
                Eintritt = ?,
                Austritt = ?,
                IDMitgliederstatus = ?,
                IDStimme = ?,
                Kammerchor = ?
            WHERE Mitgliedsnummer = ?
            """;

        int updatedRows = jdbcTemplate.update(
                sql,
                r.eintritt(),
                r.austritt(),
                r.mitgliedsstatusId(),
                r.stimmeId(),
                r.kammerchor(),
                mitgliedsnummer
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Mitglied mit Nummer " + mitgliedsnummer + " wurde nicht gefunden.");
        }
    }

    public String getCurrentMitgliedsnummerForUpdate() {
        String sql = """
            SELECT neueMitgliedsnummer
            FROM tblAllgemein_FT
            FOR UPDATE
            """;

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
            PLZ,
            Ort,
            StrasseHausNr,
            Geburtsdatum
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(
                sql,
                id,
                s != null && s.anrede() != null ? s.anrede() : MemberDefaults.DEFAULT_ANREDE,
                s != null && s.akademischerTitel() != null ? s.akademischerTitel() : MemberDefaults.DEFAULT_AKADEMISCHER_TITEL,
                s != null ? s.vorname() : null,
                s != null ? s.nachname() : null,
                s != null ? s.plz() : null,
                s != null ? s.ort() : null,
                s != null ? s.strasseHausNr() : null,
                s != null ? s.geburtsdatum() : null
        );
    }

    public void insertMitgliedschaft(String id, MitgliedschaftDto m) {
        String sql = """
        INSERT INTO tblMitgliedschaft (
            Mitgliedsnummer,
            Eintritt,
            Austritt,
            IDMitgliederstatus,
            IDStimme,
            Kammerchor
        )
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(
                sql,
                id,
                m != null ? m.eintritt() : null,
                m != null ? m.austritt() : null,
                m != null && m.mitgliedsstatusId() != null ? m.mitgliedsstatusId() : MemberDefaults.DEFAULT_MITGLIEDSSTATUS_ID,
                m != null && m.stimmeId() != null ? m.stimmeId() : MemberDefaults.DEFAULT_STIMME_ID,
                m != null && m.kammerchor() != null ? m.kammerchor() : MemberDefaults.DEFAULT_KAMMERCHOR
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
                k != null && k.briefanrede() != null ? k.briefanrede() : MemberDefaults.DEFAULT_BRIEFANREDE
        );
    }

    public void insertChorkleidung(String id) {
        String sql = """
            INSERT INTO tblChorkleidung (
                Mitgliedsnummer,
                Neubeschaffung,
                Barzahlung
         )
        VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(
                sql,
                id,
                MemberDefaults.DEFAULT_NEUBESCHAFFUNG,
                MemberDefaults.DEFAULT_BARZAHLUNG
        );
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