package de.emc.mitglieder.mapper;

import de.emc.mitglieder.dto.member.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberMapperTest {

    @Test
    void mapMemberDetail_shouldMapAllSectionsAndLocalDates() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("Mitgliedsnummer")).thenReturn("N1234");

        when(rs.getBoolean("PersonFirma")).thenReturn(false);
        when(rs.getString("Anrede")).thenReturn("Herr");
        when(rs.getString("AkademischerTitel")).thenReturn("Dr.");
        when(rs.getString("Vorname")).thenReturn("Max");
        when(rs.getString("Nachname")).thenReturn("Mustermann");
        when(rs.getString("PLZ")).thenReturn("99084");
        when(rs.getString("Ort")).thenReturn("Erfurt");
        when(rs.getString("StrasseHausNr")).thenReturn("Musterstraße 1");
        mockDate(rs, "Geburtsdatum", LocalDate.of(1980, 5, 20));

        when(rs.getString("Telefon_privat")).thenReturn("0361...");
        when(rs.getString("Telefon_geschaeftlich")).thenReturn("0361-123");
        when(rs.getString("Mobiltelefon")).thenReturn("0151...");
        when(rs.getString("EMail")).thenReturn("max@example.de");
        when(rs.getString("Adresszusatz")).thenReturn("c/o Beispiel");
        when(rs.getString("Briefanrede")).thenReturn("Lieber Sangesfreund Max Mustermann");

        mockDate(rs, "Eintritt", LocalDate.of(2024, 1, 1));
        mockDate(rs, "Austritt", LocalDate.of(2025, 12, 31));
        when(rs.getObject("IDMitgliederstatus")).thenReturn(4);
        when(rs.getString("Mitgliederstatus")).thenReturn("Kandidat");
        when(rs.getObject("IDStimme")).thenReturn(6);
        when(rs.getString("Stimme")).thenReturn("keine");
        when(rs.getObject("Kammerchor")).thenReturn(Boolean.TRUE);
        when(rs.getBoolean("Kammerchor")).thenReturn(true);

        MemberDetailDto actual = MemberMapper.mapMemberDetail(rs);

        MemberDetailDto expected = new MemberDetailDto(
                "N1234",
                new StammdatenDto(
                        false,
                        "Herr",
                        "Dr.",
                        "Max",
                        "Mustermann",
                        "99084",
                        "Erfurt",
                        "Musterstraße 1",
                        LocalDate.of(1980, 5, 20)
                ),
                new KontaktDto(
                        "0361...",
                        "0361-123",
                        "0151...",
                        "max@example.de",
                        "c/o Beispiel",
                        "Lieber Sangesfreund Max Mustermann"
                ),
                new MitgliedschaftDto(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2025, 12, 31),
                        4,
                        "Kandidat",
                        6,
                        "keine",
                        true
                )
        );

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void mapMemberDetail_shouldMapNullDatesToNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("Mitgliedsnummer")).thenReturn("N1234");

        when(rs.getBoolean("PersonFirma")).thenReturn(false);
        when(rs.getString("Anrede")).thenReturn("Herr");
        when(rs.getString("AkademischerTitel")).thenReturn(null);
        when(rs.getString("Vorname")).thenReturn("Max");
        when(rs.getString("Nachname")).thenReturn("Mustermann");
        when(rs.getString("PLZ")).thenReturn("99084");
        when(rs.getString("Ort")).thenReturn("Erfurt");
        when(rs.getString("StrasseHausNr")).thenReturn("Musterstraße 1");
        mockNullDate(rs, "Geburtsdatum");

        when(rs.getString("Telefon_privat")).thenReturn(null);
        when(rs.getString("Telefon_geschaeftlich")).thenReturn(null);
        when(rs.getString("Mobiltelefon")).thenReturn(null);
        when(rs.getString("EMail")).thenReturn(null);
        when(rs.getString("Adresszusatz")).thenReturn(null);
        when(rs.getString("Briefanrede")).thenReturn(null);

        mockNullDate(rs, "Eintritt");
        mockNullDate(rs, "Austritt");
        when(rs.getObject("IDMitgliederstatus")).thenReturn(null);
        when(rs.getString("Mitgliederstatus")).thenReturn(null);
        when(rs.getObject("IDStimme")).thenReturn(null);
        when(rs.getString("Stimme")).thenReturn(null);
        when(rs.getObject("Kammerchor")).thenReturn(null);

        MemberDetailDto actual = MemberMapper.mapMemberDetail(rs);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(new MemberDetailDto(
                        "N1234",
                        new StammdatenDto(
                                false,
                                "Herr",
                                null,
                                "Max",
                                "Mustermann",
                                "99084",
                                "Erfurt",
                                "Musterstraße 1",
                                null
                        ),
                        new KontaktDto(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        new MitgliedschaftDto(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ));
    }

    @Test
    void mapDatenschutz_shouldMapDatenschutzFields() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("Mitgliedsnummer")).thenReturn("N1234");
        mockDate(rs, "DatumDatenschutz", LocalDate.of(2026, 5, 17));
        when(rs.getBoolean("DatenschutzNr14")).thenReturn(true);
        when(rs.getBoolean("DatenschutzNr15")).thenReturn(false);
        when(rs.getBoolean("DatenschutzNr16")).thenReturn(true);
        when(rs.getBoolean("DatenschutzNr17")).thenReturn(false);
        when(rs.getBoolean("DatenschutzNr18")).thenReturn(true);

        MemberDatenschutzDto actual = MemberMapper.mapDatenschutz(rs);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(new MemberDatenschutzDto(
                        "N1234",
                        LocalDate.of(2026, 5, 17),
                        true,
                        false,
                        true,
                        false,
                        true
                ));
    }

    @Test
    void mapChorkleidung_shouldMapChorkleidungFields() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("Mitgliedsnummer")).thenReturn("N1234");
        when(rs.getString("EhemaligeStimme")).thenReturn("Tenor");
        mockDate(rs, "UebergabeAm", LocalDate.of(2024, 1, 10));
        when(rs.getString("BemerkungUebergabe")).thenReturn("übergeben");
        when(rs.getBoolean("Neubeschaffung")).thenReturn(true);
        mockDate(rs, "DatumAnteil", LocalDate.of(2024, 2, 10));
        when(rs.getBoolean("Barzahlung")).thenReturn(false);
        when(rs.getString("Bearbeitungsstand")).thenReturn("offen");
        mockDate(rs, "RueckgabeAm", LocalDate.of(2025, 1, 10));
        when(rs.getString("BemerkungRueckgabe")).thenReturn("zurückgegeben");
        mockDate(rs, "Kaufdatum", LocalDate.of(2024, 3, 10));
        when(rs.getBigDecimal("Kaufpreis")).thenReturn(new BigDecimal("123.45"));
        when(rs.getBoolean("Sommerkleidung")).thenReturn(true);
        mockDate(rs, "SommerkleidungErhalten", LocalDate.of(2024, 4, 10));
        mockDate(rs, "SommerkleidungRueckgabe", LocalDate.of(2025, 4, 10));

        MemberChorkleidungDto actual = MemberMapper.mapChorkleidung(rs);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(new MemberChorkleidungDto(
                        "N1234",
                        "Tenor",
                        LocalDate.of(2024, 1, 10),
                        "übergeben",
                        true,
                        LocalDate.of(2024, 2, 10),
                        false,
                        "offen",
                        LocalDate.of(2025, 1, 10),
                        "zurückgegeben",
                        LocalDate.of(2024, 3, 10),
                        new BigDecimal("123.45"),
                        true,
                        LocalDate.of(2024, 4, 10),
                        LocalDate.of(2025, 4, 10)
                ));
    }

    private static void mockDate(ResultSet rs, String columnName, LocalDate value) throws Exception {
        when(rs.getTimestamp(columnName)).thenReturn(Timestamp.valueOf(value.atStartOfDay()));
        when(rs.getDate(columnName)).thenReturn(Date.valueOf(value));
    }

    private static void mockNullDate(ResultSet rs, String columnName) throws Exception {
        when(rs.getTimestamp(columnName)).thenReturn(null);
    }
}