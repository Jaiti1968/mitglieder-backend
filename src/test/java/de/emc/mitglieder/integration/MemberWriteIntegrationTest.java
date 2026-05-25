package de.emc.mitglieder.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberWriteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockHttpSession loginAsEditor() throws Exception {
        String requestBody = """
                {
                  "username": "it_editor",
                  "password": "test1234"
                }
                """;

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    @Test
    void shouldUpdateStammdaten() throws Exception {
        MockHttpSession session = loginAsEditor();

        String body = """
                {
                  "personFirma": false,
                  "anrede": "Herr",
                  "vorname": "Maximilian",
                  "nachname": "Mustermann",
                  "plz": "99084",
                  "ort": "Erfurt",
                  "strasseHausNr": "Teststraße 99"
                }
                """;

        mockMvc.perform(put("/api/members/N1001/stammdaten")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        String vorname = jdbcTemplate.queryForObject(
                "SELECT Vorname FROM tblMitglieder WHERE Mitgliedsnummer = ?",
                String.class,
                "N1001"
        );

        String strasse = jdbcTemplate.queryForObject(
                "SELECT StrasseHausNr FROM tblMitglieder WHERE Mitgliedsnummer = ?",
                String.class,
                "N1001"
        );

        assertEquals("Maximilian", vorname);
        assertEquals("Teststraße 99", strasse);
    }

    @Test
    void shouldUpdateContactData() throws Exception {
        MockHttpSession session = loginAsEditor();

        String body = """
                {
                  "email": "max.neu@example.test",
                  "telefonPrivat": "0361 123456",
                  "briefanrede": "Hallo Max"
                }
                """;

        mockMvc.perform(put("/api/members/N1001/kontakt")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        String email = jdbcTemplate.queryForObject(
                "SELECT EMail FROM tblKontaktdaten WHERE Mitgliedsnummer = ?",
                String.class,
                "N1001"
        );

        assertEquals("max.neu@example.test", email);
    }

    @AfterEach
    void resetTestMember() {
        jdbcTemplate.update("""
            UPDATE tblMitglieder
            SET Vorname = ?, Nachname = ?, StrasseHausNr = ?
            WHERE Mitgliedsnummer = ?
            """,
                "Max", "Mustermann", "Teststraße 1", "N1001");

        jdbcTemplate.update("""
            UPDATE tblKontaktdaten
            SET EMail = ?, Telefon_privat = ?, Briefanrede = ?
            WHERE Mitgliedsnummer = ?
            """,
                "max.mustermann@example.test", null, "Lieber Sangesfreund Max Mustermann", "N1001");
    }
}