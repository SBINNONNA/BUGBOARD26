package com.bugboard.bugboard26.repository;

import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository Spring Data JPA per la gestione dei commenti.
 * <p>
 * Fornisce metodi per l'accesso ai dati dei commenti, inclusi
 * metodi di ricerca personalizzati per recuperare commenti
 * associati a issue specifiche o creati da determinati utenti.
 * </p>
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Trova tutti i commenti associati a una issue specifica,
     * ordinati per data di creazione crescente.
     * <p>
     * Questo metodo soddisfa il requisito 5: visualizzazione
     * dei commenti in ordine cronologico.
     * </p>
     *
     * @param issueId identificativo della issue
     * @return lista dei commenti ordinati cronologicamente
     */
    List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);

    /**
     * Trova tutti i commenti scritti da un utente specifico.
     *
     * @param author l'utente autore dei commenti
     * @return lista dei commenti dell'utente
     */
    List<Comment> findByAuthor(User author);
}