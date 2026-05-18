package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.auth.AuthUserResponse;
import de.emc.mitglieder.dto.auth.LoginRequest;
import de.emc.mitglieder.repository.user.UserRepository;
import de.emc.mitglieder.security.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        userRepository.updateLastLogin(user.getId());

        return ResponseEntity.ok(
                new AuthUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(
                new AuthUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole()
                )
        );
    }
}