package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST per la gestione dei progetti.
 * <p>
 * Espone endpoint per ottenere l'elenco dei progetti, creare un nuovo progetto,
 * eliminare un progetto e gestire i membri associati a un progetto.
 * </p>
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Crea un nuovo controller dei progetti.
     *
     * @param s servizio applicativo per la gestione dei progetti
     */
    public ProjectController(ProjectService s) {
        this.projectService = s;
    }

    /**
     * Restituisce tutti i progetti disponibili.
     *
     * @return lista dei progetti
     */
    @GetMapping
    public List<Project> getAll() {
        return projectService.getAll();
    }

    /**
     * Crea un nuovo progetto.
     * <p>
     * L'operazione è riservata agli utenti con autorizzazione di amministratore.
     * </p>
     *
     * @param body dati del nuovo progetto, inclusi nome e descrizione opzionale
     * @return il progetto creato
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Project> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(projectService.create(
                body.get("name"),
                body.getOrDefault("description", "")
        ));
    }

    /**
     * Elimina un progetto esistente.
     * <p>
     * L'operazione è riservata agli utenti con autorizzazione di amministratore.
     * </p>
     *
     * @param id identificativo del progetto da eliminare
     * @return risposta senza contenuto con stato HTTP 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restituisce i membri associati a un progetto.
     *
     * @param id identificativo del progetto
     * @return risposta contenente l'elenco dei membri del progetto
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id) {
        return projectService.getProjectMembers(id);
    }

    /**
     * Aggiunge un utente ai membri di un progetto.
     * <p>
     * L'operazione è riservata agli utenti con autorizzazione di amministratore.
     * </p>
     *
     * @param id     identificativo del progetto
     * @param userId identificativo dell'utente da aggiungere
     * @return risposta con l'esito dell'operazione
     */
    @PostMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                       @PathVariable Long userId) {
        return projectService.addMember(id, userId);
    }

    /**
     * Rimuove un utente dai membri di un progetto.
     * <p>
     * L'operazione è riservata agli utenti con autorizzazione di amministratore.
     * </p>
     *
     * @param id     identificativo del progetto
     * @param userId identificativo dell'utente da rimuovere
     * @return risposta con l'esito dell'operazione
     */
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId) {
        return projectService.removeMember(id, userId);
    }
}