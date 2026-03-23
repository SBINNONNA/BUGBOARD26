package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST per la gestione dei commenti associati alle issue.
 * <p>
 * Espone le operazioni per:
 * creare un commento, ottenere i commenti di una issue,
 * aggiornare un commento esistente ed eliminarlo.
 * </p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * Crea un nuovo controller dei commenti.
     *
     * @param commentService servizio applicativo per la gestione dei commenti
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Aggiunge un nuovo commento a una issue.
     *
     * @param projectId   identificativo del progetto
     * @param issueId     identificativo della issue
     * @param body        mappa contenente il testo del commento
     * @param userDetails utente autenticato
     * @return il commento creato con stato HTTP 201 (CREATED)
     */
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Comment comment = commentService.addComment(
                issueId, body.get("text"), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Restituisce tutti i commenti associati a una issue.
     *
     * @param projectId identificativo del progetto
     * @param issueId   identificativo della issue
     * @return lista dei commenti della issue
     */
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long issueId) {
        return ResponseEntity.ok(commentService.getCommentsByIssue(issueId));
    }

    /**
     * Aggiorna il testo di un commento esistente.
     * <p>
     * L'operazione è consentita solo all'autore del commento o a un amministratore.
     * </p>
     *
     * @param projectId   identificativo del progetto
     * @param issueId     identificativo della issue
     * @param commentId   identificativo del commento
     * @param body        mappa contenente il nuovo testo del commento
     * @param userDetails utente autenticato
     * @return il commento aggiornato oppure un messaggio di errore con stato HTTP 403
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Comment updated = commentService.updateComment(
                    commentId, body.get("text"), userDetails.getUsername());
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Elimina un commento esistente.
     * <p>
     * L'operazione è consentita solo all'autore del commento o a un amministratore.
     * </p>
     *
     * @param projectId   identificativo del progetto
     * @param issueId     identificativo della issue
     * @param commentId   identificativo del commento
     * @param userDetails utente autenticato
     * @return risposta senza contenuto con stato HTTP 204 (NO_CONTENT),
     *         oppure un messaggio di errore con stato HTTP 403
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            commentService.deleteComment(commentId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}