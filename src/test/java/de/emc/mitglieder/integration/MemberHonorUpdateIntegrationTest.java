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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberHonorUpdateIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    MemberHonorUpdateIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }


    private int createHonor(
            MockHttpSession session,
            int honorTypeId,
            String honorDate
    ) throws Exception {

        String requestBody = """
            {
              "honorTypeId": %d,
              "honorDate": "%s"
            }
            """.formatted(honorTypeId, honorDate);

        String responseBody = mockMvc.perform(
                        post("/api/members/N1001/honors")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return new ObjectMapper()
                .readTree(responseBody)
                .get("honorId")
                .asInt();
    }

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
    void shouldUpdateHonorAsEditor() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
                {
                  "honorTypeId": 5,
                  "honorDate": "2026-11-17"
                }
                """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.honorId").value(honorId))
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2026-11-17"));
    }

    @Test
    void shouldRejectUpdateHonorAsViewer() throws Exception {
        MockHttpSession editorSession = loginAsEditor();

        int honorId = createHonor(
                editorSession,
                5,
                "2020-05-10"
        );

        MockHttpSession viewerSession = loginAsViewer();

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(viewerSession)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateHonorAsAdmin() throws Exception {
        MockHttpSession session = loginAsAdmin();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.honorId").value(honorId))
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2026-11-17"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingHonorForUnknownMember() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N5000/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("N5000")));
    }

    @Test
    void shouldReturnNotFoundWhenHonorBelongsToAnotherMember() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1002/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Ehrung")));
    }

    @Test
    void shouldReturnBadRequestForUnknownHonorType() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 9999,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Ehrungsart")));
    }

    @Test
    void shouldReturnConflictWhenChangingToExistingHonorType() throws Exception {
        MockHttpSession session = loginAsEditor();

        createHonor(
                session,
                5,
                "2020-05-10"
        );

        int secondHonorId = createHonor(
                session,
                6,
                "2021-06-15"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", secondHonorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenHonorTypeIdIsMissing() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRemoveHonorDate() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": null
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.honorId").value(honorId))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value(
                        org.hamcrest.Matchers.nullValue()
                ));
    }

    @Test
    void shouldAllowFutureHonorDate() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 5,
              "honorDate": "2030-05-10"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.honorId").value(honorId))
                .andExpect(jsonPath("$.honorTypeId").value(5))
                .andExpect(jsonPath("$.honorDate").value("2030-05-10"));
    }

    @Test
    void shouldChangeHonorTypeSuccessfully() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        String updateRequest = """
            {
              "honorTypeId": 6,
              "honorDate": "2026-11-17"
            }
            """;

        mockMvc.perform(
                        put("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                                .contentType("application/json")
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.honorId").value(honorId))
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.honorTypeId").value(6))
                .andExpect(jsonPath("$.honorDate").value("2026-11-17"));
    }

}
