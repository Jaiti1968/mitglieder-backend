package de.emc.mitglieder.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Benutzername ist erforderlich")
    private String username;

    @NotBlank(message = "Passwort ist erforderlich")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}