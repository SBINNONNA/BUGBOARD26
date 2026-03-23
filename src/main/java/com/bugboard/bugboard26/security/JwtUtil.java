package com.bugboard.bugboard26.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility per la generazione e la validazione dei token JWT.
 * I parametri {@code secret} ed {@code expiration} vengono
 * letti dall'application properties.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Costruisce la chiave HMAC-SHA a partire dal segreto configurato.
     *
     * @return chiave crittografica per la firma del token
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un token JWT firmato per l'utente specificato.
     *
     * @param email indirizzo email dell'utente (subject del token)
     * @return token JWT compatto
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Estrae l'email (subject) dal token JWT.
     *
     * @param token token JWT da cui estrarre il subject
     * @return email dell'utente contenuta nel token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Verifica se il token JWT è valido e non scaduto.
     *
     * @param token token JWT da validare
     * @return {@code true} se il token è valido, {@code false} altrimenti
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Effettua il parsing del token e restituisce i claims.
     *
     * @param token token JWT da analizzare
     * @return claims contenuti nel token
     * @throws JwtException se il token è malformato o scaduto
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
