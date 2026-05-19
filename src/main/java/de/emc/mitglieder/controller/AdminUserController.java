package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.admin.CreateUserRequest;
import de.emc.mitglieder.dto.admin.UpdateUserActiveRequest;
import de.emc.mitglieder.dto.admin.UpdateUserPasswordRequest;
import de.emc.mitglieder.dto.admin.UpdateUserRoleRequest;
import de.emc.mitglieder.dto.admin.UserAdminResponse;
import de.emc.mitglieder.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<UserAdminResponse> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<Void> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        adminUserService.createUser(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        adminUserService.updateRole(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<Void> updateActive(
            @PathVariable Long id,
            @RequestBody UpdateUserActiveRequest request
    ) {
        adminUserService.updateActive(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPasswordRequest request
    ) {
        adminUserService.updatePassword(id, request);
        return ResponseEntity.noContent().build();
    }
}