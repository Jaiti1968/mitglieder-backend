package de.emc.mitglieder.service.admin;

import de.emc.mitglieder.dto.admin.CreateUserRequest;
import de.emc.mitglieder.dto.admin.UpdateUserActiveRequest;
import de.emc.mitglieder.dto.admin.UpdateUserPasswordRequest;
import de.emc.mitglieder.dto.admin.UpdateUserRoleRequest;
import de.emc.mitglieder.dto.admin.UserAdminResponse;
import de.emc.mitglieder.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAllForAdmin();
    }

    public void createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Benutzername existiert bereits"
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        userRepository.createUser(
                request.username(),
                passwordHash,
                request.role()
        );
    }

    public void updateRole(Long userId, UpdateUserRoleRequest request) {
        ensureUserExists(userId);
        userRepository.updateRole(userId, request.role());
    }

    public void updateActive(Long userId, UpdateUserActiveRequest request) {
        ensureUserExists(userId);
        userRepository.updateActive(userId, request.active());
    }

    public void updatePassword(
            Long userId,
            UpdateUserPasswordRequest request
    ) {
        ensureUserExists(userId);

        String passwordHash = passwordEncoder.encode(request.password());

        userRepository.updatePassword(userId, passwordHash);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Benutzer nicht gefunden"
            );
        }
    }
}