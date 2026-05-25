package de.emc.mitglieder.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserLoginStateIntegrationTest {

    private static final String TEST_PASSWORD_HASH =
            "$2a$10$CwTycUXWue0Thq9StjUM0uJ8pYG7bqgE7VPZtB.d8iR6P1Y2qQh1e";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanupTestUsers() {
        jdbcTemplate.update("""
                DELETE FROM tblUsers
                WHERE username LIKE 'it_login_%'
                """);
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        String requestBody = """
                {
                  "username": "it_admin",
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

    private void createTestUser(String username, boolean active) {
        jdbcTemplate.update("""
                INSERT INTO tblUsers (username, password_hash, role, active)
                VALUES (?, ?, ?, ?)
                """,
                username,
                TEST_PASSWORD_HASH,
                "VIEWER",
                active
        );
    }

    private Long getUserId(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM tblUsers WHERE username = ?",
                Long.class,
                username
        );
    }

    @Test
    void inactiveUserShouldNotLogin() throws Exception {
        createTestUser("it_login_inactive_user", false);

        String requestBody = """
                {
                  "username": "it_login_inactive_user",
                  "password": "test1234"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userShouldNotLoginWithOldPasswordAfterPasswordChange() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();

        createTestUser("it_login_password_user", true);
        Long userId = getUserId("it_login_password_user");

        String changePasswordBody = """
                {
                  "password": "newTest1234"
                }
                """;

        mockMvc.perform(put("/api/admin/users/{id}/password", userId)
                        .session(adminSession)
                        .contentType("application/json")
                        .content(changePasswordBody))
                .andExpect(status().isNoContent());

        String loginWithOldPassword = """
                {
                  "username": "it_login_password_user",
                  "password": "test1234"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginWithOldPassword))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userShouldLoginWithNewPasswordAfterPasswordChange() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();

        createTestUser("it_login_new_password_user", true);
        Long userId = getUserId("it_login_new_password_user");

        String changePasswordBody = """
                {
                  "password": "newTest1234"
                }
                """;

        mockMvc.perform(put("/api/admin/users/{id}/password", userId)
                        .session(adminSession)
                        .contentType("application/json")
                        .content(changePasswordBody))
                .andExpect(status().isNoContent());

        String loginWithNewPassword = """
                {
                  "username": "it_login_new_password_user",
                  "password": "newTest1234"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginWithNewPassword))
                .andExpect(status().isOk());
    }
}