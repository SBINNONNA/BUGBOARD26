package com.bugboard.bugboard26.controller;
import java.security.Principal;
import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.service.IssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller REST per la gestione delle issue di un progetto.
 * <p>
 * Espone endpoint per creazione, ricerca, modifica, assegnazione,
 * impostazione della scadenza, completamento ed eliminazione delle issue.
 * </p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    /**
     * Crea un nuovo controller delle issue.
     *
     * @param issueService servizio applicativo per la gestione delle issue
     */
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Crea una nuova issue all'interno di un progetto.
     *
     * @param projectId   identificativo del progetto
     * @param body        dati della issue in formato chiave/valore
     * @param userDetails utente autenticato che richiede l'operazione
     * @return la issue creata con stato HTTP 201 (CREATED)
     */
    @PostMapping
    public ResponseEntity<Issue> createIssue(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long assignedToId = null;
        String a = body.get("assignedToId");
        if (a != null && !a.isEmpty()) assignedToId = Long.parseLong(a);

        Issue issue = issueService.createIssue(
                projectId,
                body.get("title"),
                body.get("description"),
                Issue.IssueType.valueOf(body.get("type").toUpperCase()),
                Issue.Priority.valueOf(body.get("priority").toUpperCase()),
                userDetails.getUsername(),
                assignedToId,
                body.get("imageUrl")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    /**
     * Restituisce l'elenco delle issue del progetto applicando eventuali filtri.
     *
     * @param projectId identificativo del progetto
     * @param keyword   termine di ricerca su titolo e descrizione
     * @param type      filtro per tipo issue
     * @param status    filtro per stato issue
     * @param priority  filtro per priorità issue
     * @return lista delle issue filtrate
     */
    @GetMapping
    public ResponseEntity<List<Issue>> getIssues(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        Issue.IssueType  issueType     = type     != null ? Issue.IssueType.valueOf(type.toUpperCase())    : null;
        Issue.Status     issueStatus   = status   != null ? Issue.Status.valueOf(status.toUpperCase())     : null;
        Issue.Priority   issuePriority = priority != null ? Issue.Priority.valueOf(priority.toUpperCase()) : null;
        return ResponseEntity.ok(issueService.getIssues(projectId, keyword, issueType, issueStatus, issuePriority));
    }

    /**
     * Restituisce una singola issue in base al suo identificativo.
     *
     * @param projectId identificativo del progetto
     * @param id        identificativo della issue
     * @return la issue richiesta
     */
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssue(@PathVariable Long projectId, @PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    /**
     * Aggiorna i dati principali di una issue esistente.
     *
     * @param projectId   identificativo del progetto
     * @param id          identificativo della issue
     * @param body        dati aggiornati della issue
     * @param userDetails utente autenticato che richiede l'operazione
     * @return la issue aggiornata
     */
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Issue.Status status = body.get("status") != null ?
                Issue.Status.valueOf(body.get("status").toUpperCase()) : null;
        Issue updated = issueService.updateIssue(
                id, body.get("title"), body.get("description"),
                status, userDetails.getUsername(),
                body.get("imageUrl"),
                body.get("deadline"));
        return ResponseEntity.ok(updated);
    }

    /**
     * Assegna una issue a un utente.
     *
     * @param projectId   identificativo del progetto
     * @param id          identificativo della issue
     * @param body        contenuto della richiesta con l'identificativo dell'utente assegnatario
     * @param userDetails utente autenticato che richiede l'operazione
     * @return la issue aggiornata
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Issue> assignIssue(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(body.get("userId"));
        Issue updated = issueService.assignIssue(id, userId, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    /**
     * Imposta la data di scadenza di una issue.
     *
     * @param projectId   identificativo del progetto
     * @param id          identificativo della issue
     * @param body        contenuto della richiesta con la data di scadenza
     * @param userDetails utente autenticato che richiede l'operazione
     * @return la issue aggiornata
     */
    @PatchMapping("/{id}/deadline")
    public ResponseEntity<Issue> setDeadline(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDateTime deadline = LocalDateTime.parse(body.get("deadline"));
        Issue updated = issueService.setDeadline(id, deadline, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    /**
     * Segna una issue come completata.
     *
     * @param projectId identificativo del progetto
     * @param issueId   identificativo della issue
     * @param principal principale autenticato
     * @return esito dell'operazione con la issue aggiornata
     */
    @PatchMapping("/{issueId}/complete")
    public ResponseEntity<?> completeIssue(@PathVariable Long projectId,
                                           @PathVariable Long issueId,
                                           Principal principal) {
        return ResponseEntity.ok(issueService.completeIssue(issueId, principal.getName()));
    }

    /**
     * Elimina una issue completata.
     *
     * @param projectId   identificativo del progetto
     * @param id          identificativo della issue
     * @param userDetails utente autenticato che richiede l'operazione
     * @return risposta senza contenuto con stato HTTP 204 (NO_CONTENT),
     *         oppure un messaggio di errore con stato HTTP 403
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIssue(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            issueService.deleteIssue(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


}