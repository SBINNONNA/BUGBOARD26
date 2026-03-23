package com.bugboard.bugboard26.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.repository.CommentRepository;
import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.repository.ProjectRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller REST per la gestione degli utenti.
 * <p>
 * Espone endpoint per ottenere il profilo dell'utente autenticato,
 * elencare gli utenti, creare, aggiornare ed eliminare utenti e
 * aggiornare la foto profilo.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository    userRepo;
    private final PasswordEncoder   passwordEncoder;
    private final IssueRepository   issueRepo;
    private final CommentRepository commentRepo;
    private final ProjectRepository projectRepo;

    /**
     * Crea un nuovo controller utenti.
     *
     * @param userRepo         repository degli utenti
     * @param passwordEncoder  encoder per la cifratura delle password
     * @param issueRepo        repository delle issue
     * @param commentRepo      repository dei commenti
     * @param projectRepo      repository dei progetti
     */
    public UserController(UserRepository userRepo,
                          PasswordEncoder passwordEncoder,
                          IssueRepository issueRepo,
                          CommentRepository commentRepo,
                          ProjectRepository projectRepo) {
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.issueRepo       = issueRepo;
        this.commentRepo     = commentRepo;
        this.projectRepo     = projectRepo;
    }

    /**
     * Restituisce l'utente autenticato corrente.
     *
     * @param principal principale autenticato
     * @return l'utente corrispondente all'email del principal
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        return ResponseEntity.ok(user);
    }

    /**
     * Restituisce tutti gli utenti registrati.
     * <p>
     * L'operazione è riservata agli amministratori.
     * </p>
     *
     * @return lista di tutti gli utenti
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    /**
     * Crea un nuovo utente.
     * <p>
     * L'operazione è riservata agli amministratori.
     * </p>
     *
     * @param body dati dell'utente da creare, inclusi email, password e ruolo opzionale
     * @return l'utente creato oppure un messaggio di errore
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        if (userRepo.findByEmail(body.get("email")).isPresent())
            return ResponseEntity.badRequest().body("Email già in uso");

        User.Role role = User.Role.UNASSIGNED_USER;
        if (body.containsKey("role")) {
            try {
                role = User.Role.valueOf(body.get("role").toUpperCase());
                if (role == User.Role.ASSIGNED_USER)
                    return ResponseEntity.badRequest().body("Ruolo non valido per la creazione");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Ruolo non riconosciuto");
            }
        }

        User u = new User();
        u.setEmail(body.get("email"));
        u.setPassword(passwordEncoder.encode(body.get("password")));
        u.setRole(role);

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userRepo.save(u));
        } catch (jakarta.validation.ConstraintViolationException e) {
            return ResponseEntity.badRequest().body("Email non valida o dati mancanti");
        }
    }

    /**
     * Aggiorna email e/o ruolo di un utente.
     * <p>
     * L'operazione è riservata agli amministratori.
     * </p>
     *
     * @param id   identificativo dell'utente da aggiornare
     * @param body dati da modificare
     * @return l'utente aggiornato
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("role"))  user.setRole(User.Role.valueOf(body.get("role")));
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    /**
     * Elimina un utente e rimuove i suoi riferimenti da commenti, issue e progetti.
     * <p>
     * L'operazione è riservata agli amministratori.
     * </p>
     *
     * @param id identificativo dell'utente da eliminare
     * @return risposta senza contenuto con stato HTTP 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // 1. Cancella tutti i commenti scritti dall'utente
        commentRepo.deleteAll(commentRepo.findByAuthor(user));

        // 2. Azzera i riferimenti nelle issue
        for (Issue issue : issueRepo.findAll()) {
            boolean changed = false;
            if (user.equals(issue.getAssignedTo())) {
                issue.setAssignedTo(null);
                issue.setStatus(Issue.Status.TODO);
                changed = true;
            }
            if (user.equals(issue.getCreatedBy())) {
                issue.setCreatedBy(null);
                changed = true;
            }
            if (changed) issueRepo.save(issue);
        }

        // 3. Rimuovi l'utente da tutti i progetti in cui è membro
        for (Project project : projectRepo.findAll()) {
            if (project.getMembers().remove(user)) {
                projectRepo.save(project);
            }
        }

        // 4. Cancella l'utente
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggiorna la foto profilo dell'utente autenticato.
     *
     * @param body        mappa contenente l'URL della nuova immagine
     * @param userDetails  dettagli dell'utente autenticato
     * @return l'utente aggiornato con la nuova foto profilo
     */
    @PutMapping("/me/picture")
    public ResponseEntity<User> updatePicture(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        user.setProfilePicture(body.get("profilePicture"));
        return ResponseEntity.ok(userRepo.save(user));
    }
}