package de.emc.mitglieder.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberHonorCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession loginAsEditor() throws Exception {
        String requestBody = """
                {
                  "username": "it_editor",
                  "password": "test1234"
                }
                """;

        return (MockHttpSession) mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(requestBody)
                )
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

        return (MockHttpSession) mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        String requestBody = """
            {
              "username": "it_admin",
              "password": "test1234"
            }
            """;

        return (MockHttpSession) mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    @Test
    void shouldCreateHonorAsEditor() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
                {
                  "honorTypeId": 5,
                  "honorDate": "2026-11-17"
                }
                """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.honorId").isNumber())
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2026-11-17"));
    }

    @Test
    void shouldRejectCreateHonorAsViewer() throws Exception {
        MockHttpSession session = loginAsViewer();

        String requestBody = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenCreatingHonorForUnknownMember() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N5000/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("N5000")));
    }

    @Test
    void shouldReturnBadRequestForUnknownHonorType() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
            {
              "honorTypeId": 9999,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Ehrungsart")));
    }

    @Test
    void shouldReturnConflictForDuplicateHonorType() throws Exception {
        MockHttpSession session = loginAsEditor();

        String firstRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2020-05-10"
            }
            """;

        String secondRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(secondRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "bereits zugeordnet"
                        )));
    }

    @Test
    void shouldReturnBadRequestWhenHonorTypeIdIsMissing() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
            {
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validierungsfehler"))
                .andExpect(jsonPath("$.validationErrors[0].field")
                        .value("honorTypeId"));
    }

    @Test
    void shouldCreateHonorAsAdmin() throws Exception {
        MockHttpSession session = loginAsAdmin();

        String requestBody = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.honorId").isNumber())
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2026-11-17"));
    }

    @Test
    void shouldCreateHonorWithoutDate() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
            {
              "honorTypeId": 5
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.honorId").isNumber())
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value(
                        org.hamcrest.Matchers.nullValue()
                ));
    }

    @Test
    void shouldCreateHonorWithFutureDate() throws Exception {
        MockHttpSession session = loginAsEditor();

        String requestBody = """
            {
              "honorTypeId": 5,
              "honorDate": "2030-06-15"
            }
            """;

        mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.honorId").isNumber())
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2030-06-15"));
    }

}
