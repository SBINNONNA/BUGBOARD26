package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import com.bugboard.bugboard26.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servizio per la gestione dell'autenticazione e della registrazione utenti.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Costruttore con iniezione delle dipendenze.
     *
     * @param userRepository       repository per l'accesso agli utenti
     * @param passwordEncoder      encoder per la cifratura delle password
     * @param jwtUtil              utility per la generazione del token JWT
     * @param authenticationManager gestore dell'autenticazione Spring
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Autentica un utente tramite email e password e genera un token JWT.
     *
     * @param email    email dell'utente
     * @param password password in chiaro
     * @return token JWT generato per l'utente autenticato
     * @throws org.springframework.security.core.AuthenticationException
     *         se le credenziali non sono valide
     */
    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return jwtUtil.generateToken(email);
    }

    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param email    email del nuovo utente
     * @param password password in chiaro (verrà cifrata con BCrypt)
     * @param role     ruolo assegnato al nuovo utente
     * @return l'entità {@link User} salvata nel database
     * @throws RuntimeException se l'email è già in uso
     */
    public User register(String email, String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email già in uso");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }
}
