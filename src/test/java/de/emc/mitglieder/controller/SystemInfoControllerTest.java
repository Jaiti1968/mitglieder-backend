package de.emc.mitglieder.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemInfoController.class)
class SystemInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildProperties buildProperties;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBackendVersionWhenAuthenticated() throws Exception {
        when(buildProperties.getVersion()).thenReturn("1.1.1-SNAPSHOT");

        mockMvc.perform(
                        get("/api/system/info")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.backendVersion").value("1.1.1-SNAPSHOT"));
    }
}