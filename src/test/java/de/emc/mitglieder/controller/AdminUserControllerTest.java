package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.admin.UserAdminResponse;
import de.emc.mitglieder.service.admin.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    void getAllUsersReturnsUserList() throws Exception {
        when(adminUserService.getAllUsers())
                .thenReturn(List.of(
                        new UserAdminResponse(1L, "admin", "ADMIN", true, null, null),
                        new UserAdminResponse(2L, "editor", "EDITOR", true, null, null)
                ));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].username").value("editor"))
                .andExpect(jsonPath("$[1].role").value("EDITOR"));
    }

    @Test
    void createUserReturnsNoContent() throws Exception {
        doNothing().when(adminUserService).createUser(any());

        mockMvc.perform(post("/api/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "password": "password123",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUserReturnsBadRequestWhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "123",
                                  "role": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRoleReturnsNoContent() throws Exception {
        doNothing().when(adminUserService).updateRole(eq(1L), any());

        mockMvc.perform(put("/api/admin/users/1/role")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "EDITOR"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateActiveReturnsNoContent() throws Exception {
        doNothing().when(adminUserService).updateActive(eq(1L), any());

        mockMvc.perform(put("/api/admin/users/1/active")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePasswordReturnsNoContent() throws Exception {
        doNothing().when(adminUserService).updatePassword(eq(1L), any());

        mockMvc.perform(put("/api/admin/users/1/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "newpassword123"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePasswordReturnsBadRequestWhenValidationFails() throws Exception {
        mockMvc.perform(put("/api/admin/users/1/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}