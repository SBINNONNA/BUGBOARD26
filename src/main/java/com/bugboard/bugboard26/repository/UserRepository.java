package com.bugboard.bugboard26.repository;

import com.bugboard.bugboard26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository Spring Data JPA per la gestione degli utenti.
 * <p>
 * Fornisce metodi per l'accesso ai dati degli utenti, inclusi
 * metodi di ricerca personalizzati basati sull'email che è
 * l'identificativo univoco utilizzato per l'autenticazione.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trova un utente tramite il suo indirizzo email.
     * <p>
     * L'email è un campo univoco nel sistema e viene utilizzata
     * come identificativo principale per l'autenticazione e
     * l'identificazione degli utenti.
     * </p>
     *
     * @param email indirizzo email dell'utente da cercare
     * @return Optional contenente l'utente se trovato, vuoto altrimenti
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se esiste già un utente con l'email specificata.
     * <p>
     * Utile per validare l'unicità dell'email durante la
     * registrazione di nuovi utenti o la modifica del profilo.
     * </p>
     *
     * @param email indirizzo email da verificare
     * @return true se esiste già un utente con questa email, false altrimenti
     */
    boolean existsByEmail(String email);
}