package de.emc.mitglieder.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession login(String username, String password) throws Exception {
        String requestBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    @Test
    void anonymousUserShouldNotAccessMembers() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerShouldAccessMembersList() throws Exception {
        MockHttpSession session = login("it_viewer", "test1234");

        mockMvc.perform(get("/api/members")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    void viewerShouldNotUpdateStammdaten() throws Exception {
        MockHttpSession session = login("it_viewer", "test1234");

        String body = """
                {
                  "personFirma": false,
                  "vorname": "Test",
                  "nachname": "User"
                }
                """;

        mockMvc.perform(put("/api/members/N9999/stammdaten")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorShouldBeAllowedToUpdateStammdaten() throws Exception {
        MockHttpSession session = login("it_editor", "test1234");

        String body = """
                {
                  "personFirma": false,
                  "vorname": "Test",
                  "nachname": "User"
                }
                """;

        mockMvc.perform(put("/api/members/N9999/stammdaten")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }
}