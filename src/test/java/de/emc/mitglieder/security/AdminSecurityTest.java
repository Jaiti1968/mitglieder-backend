package de.emc.mitglieder.security;

import de.emc.mitglieder.dto.admin.UserAdminResponse;
import de.emc.mitglieder.repository.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        AppUserDetails adminUser = new AppUserDetails(
                1L,
                "admin",
                passwordEncoder.encode("admin123"),
                "ADMIN",
                true
        );

        AppUserDetails editorUser = new AppUserDetails(
                2L,
                "editor",
                passwordEncoder.encode("editor123"),
                "EDITOR",
                true
        );

        AppUserDetails viewerUser = new AppUserDetails(
                3L,
                "viewer",
                passwordEncoder.encode("viewer123"),
                "VIEWER",
                true
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminUser));

        when(userRepository.findByUsername("editor"))
                .thenReturn(Optional.of(editorUser));

        when(userRepository.findByUsername("viewer"))
                .thenReturn(Optional.of(viewerUser));

        when(userRepository.findAllForAdmin())
                .thenReturn(List.of(
                        new UserAdminResponse(1L, "admin", "ADMIN", true, null, null),
                        new UserAdminResponse(2L, "editor", "EDITOR", true, null, null),
                        new UserAdminResponse(3L, "viewer", "VIEWER", true, null, null)
                ));
    }

    @Test
    void adminCanAccessAdminUsers() throws Exception {
        HttpSession session = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/users")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk());
    }

    @Test
    void editorCannotAccessAdminUsers() throws Exception {
        HttpSession session = login("editor", "editor123");

        mockMvc.perform(get("/api/admin/users")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotAccessAdminUsers() throws Exception {
        HttpSession session = login("viewer", "viewer123");

        mockMvc.perform(get("/api/admin/users")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isForbidden());
    }

    private HttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getRequest().getSession(false);
    }
}