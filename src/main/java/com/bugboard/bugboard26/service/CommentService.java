package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.CommentRepository;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servizio per la gestione dei commenti associati alle issue.
 * Supporta creazione, lettura, modifica ed eliminazione.
 * Le operazioni di modifica ed eliminazione sono consentite
 * solo all'autore del commento o a un utente con ruolo {@code ADMIN}.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository   issueRepository;
    private final UserRepository    userRepository;

    /**
     * Costruttore con iniezione delle dipendenze.
     *
     * @param commentRepository repository per i commenti
     * @param issueRepository   repository per le issue
     * @param userRepository    repository per gli utenti
     */
    public CommentService(CommentRepository commentRepository,
                          IssueRepository issueRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository   = issueRepository;
        this.userRepository    = userRepository;
    }

    /**
     * Aggiunge un nuovo commento a una issue.
     *
     * @param issueId     ID della issue a cui aggiungere il commento
     * @param text        testo del commento
     * @param authorEmail email dell'utente autore
     * @return il commento salvato
     * @throws RuntimeException se la issue o l'utente non esistono
     */
    public Comment addComment(Long issueId, String text, String authorEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Comment comment = new Comment();
        comment.setText(text);
        comment.setIssue(issue);
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }

    /**
     * Restituisce tutti i commenti di una issue, ordinati per data di creazione.
     *
     * @param issueId ID della issue
     * @return lista di commenti in ordine cronologico crescente
     */
    public List<Comment> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }

    /**
     * Modifica il testo di un commento esistente.
     * Consentito solo all'autore del commento o a un {@code ADMIN}.
     *
     * @param commentId       ID del commento da modificare
     * @param newText         nuovo testo del commento
     * @param requestingEmail email dell'utente che richiede la modifica
     * @return il commento aggiornato
     * @throws RuntimeException    se il commento o l'utente non esistono
     * @throws SecurityException   se l'utente non è autorizzato
     */
    public Comment updateComment(Long commentId, String newText, String requestingEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commento non trovato"));
        User requester = userRepository.findByEmail(requestingEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isOwner = comment.getAuthor().getEmail().equals(requestingEmail);
        boolean isAdmin = "ADMIN".equals(requester.getRole().name());

        if (!isOwner && !isAdmin)
            throw new SecurityException("Non autorizzato a modificare questo commento");

        comment.setText(newText);
        return commentRepository.save(comment);
    }

    /**
     * Elimina un commento esistente.
     * Consentito solo all'autore del commento o a un {@code ADMIN}.
     *
     * @param commentId       ID del commento da eliminare
     * @param requestingEmail email dell'utente che richiede l'eliminazione
     * @throws RuntimeException    se il commento o l'utente non esistono
     * @throws SecurityException   se l'utente non è autorizzato
     */
    public void deleteComment(Long commentId, String requestingEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commento non trovato"));
        User requester = userRepository.findByEmail(requestingEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isOwner = comment.getAuthor().getEmail().equals(requestingEmail);
        boolean isAdmin = "ADMIN".equals(requester.getRole().name());

        if (!isOwner && !isAdmin)
            throw new SecurityException("Non autorizzato a eliminare questo commento");

        commentRepository.delete(comment);
    }
}
