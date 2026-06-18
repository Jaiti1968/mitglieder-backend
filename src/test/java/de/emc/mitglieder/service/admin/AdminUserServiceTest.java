package de.emc.mitglieder.service.admin;

import de.emc.mitglieder.dto.admin.UpdateUserActiveRequest;
import de.emc.mitglieder.dto.admin.UpdateUserRoleRequest;
import de.emc.mitglieder.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void updateActiveShouldRejectDeactivatingLastActiveAdmin() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.isActiveAdmin(1L)).thenReturn(true);
        when(userRepository.countActiveAdmins()).thenReturn(1);

        assertThrows(
                ResponseStatusException.class,
                () -> adminUserService.updateActive(
                        1L,
                        new UpdateUserActiveRequest(false)
                )
        );

        verify(userRepository, never()).updateActive(1L, false);
    }

    @Test
    void updateRoleShouldRejectDowngradingLastActiveAdmin() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.isActiveAdmin(1L)).thenReturn(true);
        when(userRepository.countActiveAdmins()).thenReturn(1);

        assertThrows(
                ResponseStatusException.class,
                () -> adminUserService.updateRole(
                        1L,
                        new UpdateUserRoleRequest("EDITOR")
                )
        );

        verify(userRepository, never()).updateRole(1L, "EDITOR");
    }

    @Test
    void updateActiveShouldAllowDeactivatingAdminWhenAnotherActiveAdminExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.isActiveAdmin(1L)).thenReturn(true);
        when(userRepository.countActiveAdmins()).thenReturn(2);

        adminUserService.updateActive(
                1L,
                new UpdateUserActiveRequest(false)
        );

        verify(userRepository).updateActive(1L, false);
    }

    @Test
    void updateRoleShouldAllowDowngradingAdminWhenAnotherActiveAdminExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.isActiveAdmin(1L)).thenReturn(true);
        when(userRepository.countActiveAdmins()).thenReturn(2);

        adminUserService.updateRole(
                1L,
                new UpdateUserRoleRequest("EDITOR")
        );

        verify(userRepository).updateRole(1L, "EDITOR");
    }
}