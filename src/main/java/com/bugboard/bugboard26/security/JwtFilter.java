package com.bugboard.bugboard26.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT per l'autenticazione basata su token nelle richieste HTTP.
 * <p>
 * Questo filtro intercetta ogni richiesta HTTP e verifica la presenza
 * di un token JWT nell'header Authorization. Se il token è valido,
 * configura il contesto di sicurezza di Spring per autenticare l'utente.
 * </p>
 * <p>
 * Il filtro estende {@code OncePerRequestFilter} per garantire che
 * venga eseguito una sola volta per ogni richiesta HTTP.
 * </p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Costruttore che inizializza le dipendenze necessarie.
     *
     * @param jwtUtil utility per la gestione dei token JWT
     * @param userDetailsService servizio per il caricamento dei dettagli utente
     */
    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Elabora ogni richiesta HTTP per verificare e validare il token JWT.
     * <p>
     * Il processo di autenticazione include:
     * <ol>
     *   <li>Estrazione dell'header Authorization</li>
     *   <li>Verifica del formato "Bearer token"</li>
     *   <li>Validazione del token JWT</li>
     *   <li>Estrazione dell'email dal token</li>
     *   <li>Caricamento dei dettagli utente</li>
     *   <li>Configurazione del contesto di sicurezza</li>
     * </ol>
     * </p>
     * <p>
     * Se il token non è presente, non è valido o è malformato,
     * la richiesta procede senza autenticazione.
     * </p>
     *
     * @param request  richiesta HTTP in ingresso
     * @param response risposta HTTP in uscita
     * @param chain    catena dei filtri per continuare l'elaborazione
     * @throws ServletException in caso di errori durante l'elaborazione del servlet
     * @throws IOException      in caso di errori di I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Estrazione dell'header Authorization
        String header = request.getHeader("Authorization");

        // Verifica presenza e formato corretto del token
        if (header != null && header.startsWith("Bearer ")) {
            // Rimozione del prefisso "Bearer " (7 caratteri)
            String token = header.substring(7);

            // Validazione del token JWT
            if (jwtUtil.isTokenValid(token)) {
                // Estrazione dell'email (subject) dal token
                String email = jwtUtil.extractEmail(token);

                // Caricamento dei dettagli utente tramite email
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Creazione del token di autenticazione per Spring Security
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Configurazione del contesto di sicurezza
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Continua la catena dei filtri
        chain.doFilter(request, response);
    }
}