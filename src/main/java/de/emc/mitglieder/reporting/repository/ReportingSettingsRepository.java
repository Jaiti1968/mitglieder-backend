package de.emc.mitglieder.reporting.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportingSettingsRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportingSettingsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int findCurrentBusinessYear() {
        String sql = """
                SELECT `Geschäftsjahr`
                FROM tblAllgemein_FT
                """;

        List<String> businessYears =
                jdbcTemplate.queryForList(sql, String.class);

        if (businessYears.isEmpty()) {
            throw new IllegalStateException(
                    "tblAllgemein_FT enthält keinen Datensatz."
            );
        }

        if (businessYears.size() > 1) {
            throw new IllegalStateException(
                    "tblAllgemein_FT enthält mehrere Datensätze. "
                            + "Fachlich ist genau ein Datensatz zulässig."
            );
        }

        String businessYear = businessYears.get(0);

        if (businessYear == null || businessYear.isBlank()) {
            throw new IllegalStateException(
                    "Das Geschäftsjahr in tblAllgemein_FT ist leer."
            );
        }

        if (!businessYear.matches("\\d{4}")) {
            throw new IllegalStateException(
                    "Das Geschäftsjahr muss genau vier Ziffern enthalten: "
                            + businessYear
            );
        }

        try {
            return Integer.parseInt(businessYear);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Das Geschäftsjahr in tblAllgemein_FT ist ungültig: "
                            + businessYear,
                    exception
            );
        }
    }
}