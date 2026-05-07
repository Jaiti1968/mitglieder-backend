package de.emc.mitglieder.mapper;

import de.emc.mitglieder.dto.member.*;

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

    private static java.time.LocalDate toLocalDate(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName) != null
                ? rs.getTimestamp(columnName).toLocalDateTime().toLocalDate()
                : null;
    }

    public static MemberDatenschutzDto mapDatenschutz(ResultSet rs) throws SQLException {
        return new MemberDatenschutzDto(
                rs.getString("Mitgliedsnummer"),
                rs.getTimestamp("DatumDatenschutz") != null
                        ? rs.getTimestamp("DatumDatenschutz").toLocalDateTime()
                        : null,
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
                rs.getString("ehemaligeStimme"),
                rs.getTimestamp("UebergabeAm") != null
                        ? rs.getTimestamp("UebergabeAm").toLocalDateTime()
                        : null,
                rs.getString("BemerkungUebergabe"),
                rs.getBoolean("Neubeschaffung"),
                rs.getTimestamp("DatumAnteil") != null
                        ? rs.getTimestamp("DatumAnteil").toLocalDateTime()
                        : null,
                rs.getBoolean("Barzahlung"),
                rs.getString("Bearbeitungsstand"),
                rs.getTimestamp("RueckgabeAm") != null
                        ? rs.getTimestamp("RueckgabeAm").toLocalDateTime()
                        : null,
                rs.getString("BemerkungRueckgabe"),
                rs.getTimestamp("Kaufdatum") != null
                        ? rs.getTimestamp("Kaufdatum").toLocalDateTime()
                        : null,
                rs.getBigDecimal("Kaufpreis"),
                rs.getBoolean("Sommerkleidung"),
                rs.getTimestamp("SommerkleidungErhalten") != null
                        ? rs.getTimestamp("SommerkleidungErhalten").toLocalDateTime()
                        : null,
                rs.getTimestamp("SommerkleidungRueckgabe") != null
                        ? rs.getTimestamp("SommerkleidungRueckgabe").toLocalDateTime()
                        : null
        );
    }
}