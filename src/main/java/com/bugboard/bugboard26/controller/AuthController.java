package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST per la gestione dell'autenticazione.
 * Espone gli endpoint di login e registrazione utente.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Costruttore con iniezione del servizio di autenticazione.
     *
     * @param authService servizio che gestisce login e registrazione
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica un utente e restituisce un token JWT.
     * Endpoint pubblico, non richiede autenticazione.
     *
     * @param body mappa contenente {@code email} e {@code password}
     * @return {@code 200 OK} con il token JWT generato
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("email"), body.get("password"));
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Registra un nuovo utente nel sistema.
     * Accessibile esclusivamente dagli utenti con ruolo {@code ADMIN}.
     *
     * @param body mappa contenente {@code email}, {@code password} e {@code role}
     * @return {@code 200 OK} con id, email e ruolo dell'utente creato
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        User.Role role = User.Role.valueOf(body.get("role").toUpperCase());
        User user = authService.register(body.get("email"), body.get("password"), role);
        return ResponseEntity.ok(Map.of(
                "id",    user.getId(),
                "email", user.getEmail(),
                "role",  user.getRole()
        ));
    }
}
