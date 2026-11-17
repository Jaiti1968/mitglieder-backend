package de.emc.mitglieder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberHonorDeleteIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    MemberHonorDeleteIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
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

    @Test
    void shouldDeleteHonorAsEditor() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        mockMvc.perform(
                        delete("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                )
                .andExpect(status().isNoContent());


        mockMvc.perform(
                        get("/api/members/N1001/honors")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectDeleteHonorAsViewer() throws Exception {
        MockHttpSession editorSession = loginAsEditor();

        int honorId = createHonor(
                editorSession,
                5,
                "2020-05-10"
        );

        MockHttpSession viewerSession = loginAsViewer();

        mockMvc.perform(
                        delete("/api/members/N1001/honors/{honorId}", honorId)
                                .session(viewerSession)
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/members/N1001/honors")
                                .session(editorSession)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].honorId").value(honorId));
    }

    @Test
    void shouldDeleteHonorAsAdmin() throws Exception {
        MockHttpSession session = loginAsAdmin();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        mockMvc.perform(
                        delete("/api/members/N1001/honors/{honorId}", honorId)
                                .session(session)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/members/N1001/honors")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingHonorForUnknownMember() throws Exception {
        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        mockMvc.perform(
                        delete("/api/members/N5000/honors/{honorId}", honorId)
                                .session(session)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("N5000")));

        mockMvc.perform(
                        get("/api/members/N1001/honors")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].honorId").value(honorId));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingHonorBelongingToAnotherMember()
            throws Exception {

        MockHttpSession session = loginAsEditor();

        int honorId = createHonor(
                session,
                5,
                "2020-05-10"
        );

        mockMvc.perform(
                        delete("/api/members/N1002/honors/{honorId}", honorId)
                                .session(session)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Ehrung")));

        mockMvc.perform(
                        get("/api/members/N1001/honors")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].honorId").value(honorId));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownHonor() throws Exception {
        MockHttpSession session = loginAsEditor();

        mockMvc.perform(
                        delete("/api/members/N1001/honors/{honorId}", 999999999)
                                .session(session)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("Ehrung")));
    }

    @Test
    void shouldRejectDeletingMemberAsEditor() throws Exception {
        MockHttpSession session = loginAsEditor();

        mockMvc.perform(
                        delete("/api/members/N1001")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }
}