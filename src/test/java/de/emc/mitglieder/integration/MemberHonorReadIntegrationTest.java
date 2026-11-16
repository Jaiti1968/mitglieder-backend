
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberHonorReadIntegrationTest {

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
    void shouldReturnEmptyHonorsForExistingMember() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members/N1002/honors")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnNotFoundForUnknownMember() throws Exception {
        MockHttpSession session = loginAsViewer();

        mockMvc.perform(get("/api/members/N5000/honors")
                        .session(session))
                .andExpect(status().isNotFound());
    }
}

