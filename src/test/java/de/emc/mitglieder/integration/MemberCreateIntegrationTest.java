package de.emc.mitglieder.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanupCreatedMembers() {
        jdbcTemplate.update("""
                DELETE FROM tblMitglieder
                WHERE Nachname = 'Testmitglied'
                """);
    }

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

    private MockHttpSession loginAsViewer() throws Exception {
        String requestBody = """
            {
              "username": "it_viewer",
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
    void editorShouldCreateMemberWithDependentRecords() throws Exception {
        MockHttpSession session = loginAsEditor();

        String body = """
                {
                  "stammdaten": {
                    "personFirma": false,
                    "anrede": "Herr",
                    "vorname": "Neuer",
                    "nachname": "Testmitglied",
                    "plz": "99084",
                    "ort": "Erfurt",
                    "strasseHausNr": "Testweg 1"
                  },
                  "kontakt": {
                    "email": "neu@example.test"
                  },
                  "mitgliedschaft": {
                    "mitgliedsstatusId": 1,
                    "stimmeId": 2,
                    "kammerchor": false
                  }
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/members")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitgliedsnummer").exists())
                .andExpect(jsonPath("$.stammdaten.vorname").value("Neuer"))
                .andExpect(jsonPath("$.stammdaten.nachname").value("Testmitglied"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String mitgliedsnummer = JsonPath.read(responseBody, "$.mitgliedsnummer");

        Integer members = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblMitglieder WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer contact = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblKontaktdaten WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer membership = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblMitgliedschaft WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer datenschutz = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblDatenschutz WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer chorkleidung = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblChorkleidung WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        assertEquals(1, members);
        assertEquals(1, contact);
        assertEquals(1, membership);
        assertEquals(1, datenschutz);
        assertEquals(1, chorkleidung);
    }

    @Test
    void createMemberShouldRollbackWhenMembershipLookupIsInvalid() throws Exception {
        MockHttpSession session = loginAsEditor();

        String body = """
            {
              "stammdaten": {
                "personFirma": false,
                "anrede": "Herr",
                "vorname": "Rollback",
                "nachname": "Testmitglied",
                "plz": "99084",
                "ort": "Erfurt",
                "strasseHausNr": "Rollbackweg 1"
              },
              "kontakt": {
                "email": "rollback@example.test"
              },
              "mitgliedschaft": {
                "mitgliedsstatusId": 9999,
                "stimmeId": 2,
                "kammerchor": false
              }
            }
            """;

        mockMvc.perform(post("/api/members")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        Integer members = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblMitglieder WHERE Vorname = ? AND Nachname = ?",
                Integer.class,
                "Rollback",
                "Testmitglied"
        );

        Integer contacts = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tblKontaktdaten k
                JOIN tblMitglieder m ON m.Mitgliedsnummer = k.Mitgliedsnummer
                WHERE m.Vorname = ? AND m.Nachname = ?
                """,
                Integer.class,
                "Rollback",
                "Testmitglied"
        );

        Integer memberships = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tblMitgliedschaft ms
                JOIN tblMitglieder m ON m.Mitgliedsnummer = ms.Mitgliedsnummer
                WHERE m.Vorname = ? AND m.Nachname = ?
                """,
                Integer.class,
                "Rollback",
                "Testmitglied"
        );

        assertEquals(0, members);
        assertEquals(0, contacts);
        assertEquals(0, memberships);
    }

    @Test
    void viewerShouldNotCreateMember() throws Exception {
        MockHttpSession session = loginAsViewer();

        String body = """
            {
              "stammdaten": {
                "personFirma": false,
                "anrede": "Herr",
                "vorname": "Viewer",
                "nachname": "Testmitglied",
                "plz": "99084",
                "ort": "Erfurt",
                "strasseHausNr": "Viewerweg 1"
              },
              "kontakt": {
                "email": "viewer@example.test"
              },
              "mitgliedschaft": {
                "mitgliedsstatusId": 1,
                "stimmeId": 2,
                "kammerchor": false
              }
            }
            """;

        mockMvc.perform(post("/api/members")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMemberShouldApplyDefaultRecordsWhenContactIsMissing() throws Exception {
        MockHttpSession session = loginAsEditor();

        String body = """
            {
              "stammdaten": {
                "personFirma": false,
                "anrede": "Herr",
                "vorname": "Default",
                "nachname": "Testmitglied",
                "plz": "99084",
                "ort": "Erfurt",
                "strasseHausNr": "Defaultweg 1"
              },
              "mitgliedschaft": {
                "mitgliedsstatusId": 1,
                "stimmeId": 2,
                "kammerchor": false
              }
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/members")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitgliedsnummer").exists())
                .andExpect(jsonPath("$.stammdaten.vorname").value("Default"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String mitgliedsnummer = JsonPath.read(responseBody, "$.mitgliedsnummer");

        Integer contact = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblKontaktdaten WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer datenschutz = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblDatenschutz WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        Integer chorkleidung = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblChorkleidung WHERE Mitgliedsnummer = ?",
                Integer.class,
                mitgliedsnummer
        );

        assertEquals(1, contact);
        assertEquals(1, datenschutz);
        assertEquals(1, chorkleidung);
    }
}