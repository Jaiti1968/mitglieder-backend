package de.emc.mitglieder.mapper;

import de.emc.mitglieder.dto.member.KontaktDto;
import de.emc.mitglieder.dto.member.MemberDetailDto;
import de.emc.mitglieder.dto.member.MitgliedschaftDto;
import de.emc.mitglieder.dto.member.StammdatenDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class MemberMapper {

    private MemberMapper() {
    }

    public static MemberDetailDto mapMemberDetail(ResultSet rs) throws SQLException {
        return new MemberDetailDto(
                rs.getString("Mitgliedsnummer"),
                mapStammdaten(rs),
                mapKontakt(rs),
                mapMitgliedschaft(rs)
        );
    }

    private static StammdatenDto mapStammdaten(ResultSet rs) throws SQLException {
        return new StammdatenDto(
                rs.getString("Anrede"),
                rs.getString("AkademischerTitel"),
                rs.getString("Vorname"),
                rs.getString("Nachname"),
                rs.getString("PLZ"),
                rs.getString("Ort"),
                rs.getString("StrasseHausNr"),
                toLocalDate(rs, "Geburtsdatum")
        );
    }

    private static KontaktDto mapKontakt(ResultSet rs) throws SQLException {
        return new KontaktDto(
                rs.getString("Telefon_privat"),
                rs.getString("Telefon_geschaeftlich"),
                rs.getString("Mobiltelefon"),
                rs.getString("EMail"),
                rs.getString("Adresszusatz"),
                rs.getString("Briefanrede")
        );
    }

    private static MitgliedschaftDto mapMitgliedschaft(ResultSet rs) throws SQLException {
        return new MitgliedschaftDto(
                toLocalDate(rs, "Eintritt"),
                toLocalDate(rs, "Austritt"),
                (Integer) rs.getObject("IDMitgliederstatus"),
                rs.getString("Mitgliederstatus"),
                (Integer) rs.getObject("IDStimme"),
                rs.getString("Stimme"),
                rs.getObject("Kammerchor") != null ? rs.getBoolean("Kammerchor") : null
        );
    }

    private static java.time.LocalDate toLocalDate(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName) != null
                ? rs.getTimestamp(columnName).toLocalDateTime().toLocalDate()
                : null;
    }
}