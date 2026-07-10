package de.emc.mitglieder.reporting.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingSettingsRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnConfiguredBusinessYear() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("2026"));

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        int businessYear = repository.findCurrentBusinessYear();

        assertEquals(2026, businessYear);
    }

    @Test
    void shouldRejectMissingSettingsRow() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of());

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findCurrentBusinessYear
        );

        assertTrue(
                exception.getMessage().contains(
                        "tblAllgemein_FT enthält keinen Datensatz"
                )
        );
    }

    @Test
    void shouldRejectMultipleSettingsRows() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("2026", "2027"));

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findCurrentBusinessYear
        );

        assertTrue(
                exception.getMessage().contains(
                        "tblAllgemein_FT enthält mehrere Datensätze"
                )
        );
    }

    @Test
    void shouldRejectBlankBusinessYear() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of(" "));

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findCurrentBusinessYear
        );

        assertTrue(
                exception.getMessage().contains(
                        "Geschäftsjahr in tblAllgemein_FT ist leer"
                )
        );
    }

    @Test
    void shouldRejectBusinessYearContainingNonDigits() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("20XX"));

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findCurrentBusinessYear
        );

        assertTrue(
                exception.getMessage().contains(
                        "genau vier Ziffern"
                )
        );
    }

    @Test
    void shouldRejectBusinessYearWithLessThanFourDigits() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("202"));

        ReportingSettingsRepository repository =
                new ReportingSettingsRepository(jdbcTemplate);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findCurrentBusinessYear
        );

        assertTrue(
                exception.getMessage().contains(
                        "genau vier Ziffern"
                )
        );
    }
}