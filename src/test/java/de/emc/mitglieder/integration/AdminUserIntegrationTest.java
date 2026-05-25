package de.emc.mitglieder.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.AfterEach;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanupTestUsers() {
        jdbcTemplate.update("""
            DELETE FROM tblUsers
            WHERE username LIKE 'it_created_%'
            """);
    }

    private MockHttpSession login(String username) throws Exception {
        String requestBody = """
                {
                  "username": "%s",
                  "password": "test1234"
                }
                """.formatted(username);

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    @Test
    void adminShouldAccessUserList() throws Exception {
        MockHttpSession session = login("it_admin");

        mockMvc.perform(get("/api/admin/users")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("it_admin")))
                .andExpect(jsonPath("$[*].username", hasItem("it_editor")))
                .andExpect(jsonPath("$[*].username", hasItem("it_viewer")));
    }

    @Test
    void editorShouldNotAccessUserList() throws Exception {
        MockHttpSession session = login("it_editor");

        mockMvc.perform(get("/api/admin/users")
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerShouldNotAccessUserList() throws Exception {
        MockHttpSession session = login("it_viewer");

        mockMvc.perform(get("/api/admin/users")
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateUser() throws Exception {
        MockHttpSession session = login("it_admin");

        String body = """
            {
              "username": "it_created_user",
              "password": "test1234",
              "role": "VIEWER"
            }
            """;

        mockMvc.perform(post("/api/admin/users")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tblUsers WHERE username = ? AND role = ? AND active = 1",
                Integer.class,
                "it_created_user",
                "VIEWER"
        );

        assertEquals(1, count);
    }

    @Test
    void adminShouldChangeUserRole() throws Exception {
        MockHttpSession session = login("it_admin");

        jdbcTemplate.update("""
            INSERT INTO tblUsers (username, password_hash, role, active)
            VALUES (?, ?, ?, ?)
            """,
                "it_created_role_user",
                "$2a$10$CwTycUXWue0Thq9StjUM0uJ8pYG7bqgE7VPZtB.d8iR6P1Y2qQh1e",
                "VIEWER",
                true
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM tblUsers WHERE username = ?",
                Long.class,
                "it_created_role_user"
        );

        String body = """
            {
              "role": "EDITOR"
            }
            """;

        mockMvc.perform(put("/api/admin/users/{id}/role", userId)
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        String role = jdbcTemplate.queryForObject(
                "SELECT role FROM tblUsers WHERE id = ?",
                String.class,
                userId
        );

        assertEquals("EDITOR", role);
    }

    @Test
    void adminShouldChangeUserActiveState() throws Exception {
        MockHttpSession session = login("it_admin");

        jdbcTemplate.update("""
            INSERT INTO tblUsers (username, password_hash, role, active)
            VALUES (?, ?, ?, ?)
            """,
                "it_created_active_user",
                "$2a$10$CwTycUXWue0Thq9StjUM0uJ8pYG7bqgE7VPZtB.d8iR6P1Y2qQh1e",
                "VIEWER",
                true
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM tblUsers WHERE username = ?",
                Long.class,
                "it_created_active_user"
        );

        String body = """
            {
              "active": false
            }
            """;

        mockMvc.perform(put("/api/admin/users/{id}/active", userId)
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        Boolean active = jdbcTemplate.queryForObject(
                "SELECT active FROM tblUsers WHERE id = ?",
                Boolean.class,
                userId
        );

        assertEquals(false, active);
    }

    @Test
    void adminShouldChangeUserPassword() throws Exception {
        MockHttpSession session = login("it_admin");

        String oldHash = "$2a$10$CwTycUXWue0Thq9StjUM0uJ8pYG7bqgE7VPZtB.d8iR6P1Y2qQh1e";

        jdbcTemplate.update("""
            INSERT INTO tblUsers (username, password_hash, role, active)
            VALUES (?, ?, ?, ?)
            """,
                "it_created_password_user",
                oldHash,
                "VIEWER",
                true
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM tblUsers WHERE username = ?",
                Long.class,
                "it_created_password_user"
        );

        String body = """
            {
              "password": "newTest1234"
            }
            """;

        mockMvc.perform(put("/api/admin/users/{id}/password", userId)
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        String newHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM tblUsers WHERE id = ?",
                String.class,
                userId
        );

        assertNotEquals(oldHash, newHash);
    }

    @Test
    void editorShouldNotCreateUser() throws Exception {
        MockHttpSession session = login("it_editor");

        String body = """
            {
              "username": "it_created_forbidden_user",
              "password": "test1234",
              "role": "VIEWER"
            }
            """;

        mockMvc.perform(post("/api/admin/users")
                        .session(session)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
