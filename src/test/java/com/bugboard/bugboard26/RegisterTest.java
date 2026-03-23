package com.bugboard.bugboard26;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import com.bugboard.bugboard26.security.JwtUtil;
import com.bugboard.bugboard26.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per il metodo {@code register(String email, String password, User.Role role)}
 * di {@link AuthService}.
 *
 * Classi di equivalenza individuate:
 *  - CE1: email non esistente + ruolo USER  → registrazione riuscita
 *  - CE2: email non esistente + ruolo ADMIN → registrazione riuscita con ruolo ADMIN
 *  - CE3: email già in uso                  → RuntimeException
 */
@ExtendWith(MockitoExtension.class)
class RegisterTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtUtil               jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    /**
     * CE1: email nuova con ruolo UNASSIGNED_USER.
     * Atteso: utente salvato con email e ruolo corretti.
     */
    @Test
    void register_emailNuovaRuoloUser_salvaCorrettamente() {
        when(userRepository.existsByEmail("nuovo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");

        User saved = new User();
        saved.setEmail("nuovo@test.com");
        saved.setRole(User.Role.UNASSIGNED_USER);
        when(userRepository.save(any())).thenReturn(saved);

        User result = authService.register("nuovo@test.com", "pass123", User.Role.UNASSIGNED_USER);

        assertNotNull(result);
        assertEquals("nuovo@test.com",        result.getEmail());
        assertEquals(User.Role.UNASSIGNED_USER, result.getRole());
        verify(passwordEncoder).encode("pass123");
    }

    /**
     * CE2: email nuova con ruolo ADMIN.
     * Atteso: utente salvato con ruolo ADMIN.
     */
    @Test
    void register_emailNuovaRuoloAdmin_salvaConRuoloAdmin() {
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        User saved = new User();
        saved.setEmail("admin@test.com");
        saved.setRole(User.Role.ADMIN);
        when(userRepository.save(any())).thenReturn(saved);

        User result = authService.register("admin@test.com", "adminpass", User.Role.ADMIN);

        assertEquals(User.Role.ADMIN, result.getRole());
    }

    /**
     * CE3: email già presente nel sistema.
     * Atteso: RuntimeException con messaggio "Email già in uso", nessun salvataggio.
     */
    @Test
    void register_emailGiaInUso_lanciaEccezione() {
        when(userRepository.existsByEmail("esistente@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register("esistente@test.com", "pass", User.Role.UNASSIGNED_USER));

        assertEquals("Email già in uso", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
