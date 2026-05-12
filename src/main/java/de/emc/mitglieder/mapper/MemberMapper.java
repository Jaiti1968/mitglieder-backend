package de.emc.mitglieder.mapper;

import de.emc.mitglieder.dto.member.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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
                rs.getBoolean("PersonFirma"),
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

    private static LocalDate toLocalDate(ResultSet rs, String columnName) throws SQLException {
        return rs.getTimestamp(columnName) != null
                ? rs.getDate(columnName).toLocalDate()
                : null;
    }

    public static MemberDatenschutzDto mapDatenschutz(ResultSet rs) throws SQLException {
        return new MemberDatenschutzDto(
                rs.getString("Mitgliedsnummer"),
                toLocalDate(rs, "DatumDatenschutz"),
                rs.getBoolean("DatenschutzNr14"),
                rs.getBoolean("DatenschutzNr15"),
                rs.getBoolean("DatenschutzNr16"),
                rs.getBoolean("DatenschutzNr17"),
                rs.getBoolean("DatenschutzNr18")
        );
    }

    public static MemberChorkleidungDto mapChorkleidung(ResultSet rs) throws SQLException {
        return new MemberChorkleidungDto(
                rs.getString("Mitgliedsnummer"),
                rs.getString("EhemaligeStimme"),
                toLocalDate(rs, "UebergabeAm"),
                rs.getString("BemerkungUebergabe"),
                rs.getBoolean("Neubeschaffung"),
                toLocalDate(rs, "DatumAnteil"),
                rs.getBoolean("Barzahlung"),
                rs.getString("Bearbeitungsstand"),
                toLocalDate(rs, "RueckgabeAm"),
                rs.getString("BemerkungRueckgabe"),
                toLocalDate(rs, "Kaufdatum"),
                rs.getBigDecimal("Kaufpreis"),
                rs.getBoolean("Sommerkleidung"),
                toLocalDate(rs, "SommerkleidungErhalten"),
                toLocalDate(rs, "SommerkleidungRueckgabe")
        );
    }
}