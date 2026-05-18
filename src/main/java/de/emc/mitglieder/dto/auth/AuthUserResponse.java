package de.emc.mitglieder.dto.auth;

public class AuthUserResponse {

    private Long id;
    private String username;
    private String role;

    public AuthUserResponse() {
    }

    public AuthUserResponse(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}