package com.bugboard.bugboard26.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configurazione della sicurezza Spring Security.
 * <p>
 * Definisce la catena di filtri HTTP, la politica di sessione stateless,
 * le regole di autorizzazione e il filtro JWT.
 * </p>
 */
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    /**
     * Costruttore con iniezione del filtro JWT.
     *
     * @param jwtFilter filtro per la validazione del token JWT
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Configura la catena di filtri di sicurezza HTTP.
     * <p>
     * Disabilita CSRF, imposta la sessione come stateless,
     * permette l'accesso pubblico a {@code /api/auth/**} e {@code /uploads/**}
     * e richiede autenticazione per tutte le altre richieste.
     * </p>
     *
     * @param http configurazione della sicurezza HTTP
     * @return la catena di filtri configurata
     * @throws Exception in caso di errori di configurazione
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Bean per la codifica delle password con BCrypt.
     *
     * @return encoder BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Espone l'{@link AuthenticationManager} come bean Spring.
     *
     * @param config configurazione di autenticazione di Spring
     * @return il gestore di autenticazione
     * @throws Exception in caso di errori di configurazione
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
