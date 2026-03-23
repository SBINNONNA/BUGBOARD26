package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementazione di {@link UserDetailsService} per l'integrazione
 * con Spring Security. Carica i dettagli dell'utente dal database
 * tramite email, usata durante l'autenticazione JWT.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Costruttore con iniezione del repository utenti.
     *
     * @param userRepository repository per l'accesso agli utenti
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carica un utente tramite la sua email e lo restituisce
     * come {@link UserDetails} con il ruolo come authority.
     *
     * @param email email dell'utente da caricare
     * @return oggetto {@link UserDetails} con credenziali e autorità
     * @throws UsernameNotFoundException se nessun utente corrisponde all'email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
