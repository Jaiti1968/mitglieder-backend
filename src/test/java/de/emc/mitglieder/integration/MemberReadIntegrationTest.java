package de.emc.mitglieder.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberReadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnMembersList() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].mitgliedsnummer", hasItem("N1001")))
                .andExpect(jsonPath("$.items[*].mitgliedsnummer", hasItem("N1002")));
    }

    @Test
    void shouldReturnMemberDetail() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members/N1001")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1001"))
                .andExpect(jsonPath("$.stammdaten.vorname").value("Max"))
                .andExpect(jsonPath("$.stammdaten.nachname").value("Mustermann"))
                .andExpect(jsonPath("$.mitgliedschaft.mitgliedsstatusId").value(1))
                .andExpect(jsonPath("$.mitgliedschaft.stimmeId").value(2));
    }

    @Test
    void shouldFindMemberBySearch() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members")
                        .param("search", "Mustermann")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].mitgliedsnummer", hasItem("N1001")));
    }

    @Test
    void shouldFilterMembersByStatus() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members")
                        .param("statusId", "4")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].mitgliedsnummer", hasItem("N1002")));
    }

    @Test
    void shouldFilterMembersByVoice() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members")
                        .param("stimmeId", "2")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].mitgliedsnummer", hasItem("N1001")));
    }
}
