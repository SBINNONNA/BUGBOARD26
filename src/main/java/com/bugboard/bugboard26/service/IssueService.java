package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.repository.ProjectRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio per la gestione delle issue all'interno dei progetti.
 * Gestisce creazione, modifica, assegnazione, completamento ed eliminazione.
 * Aggiorna automaticamente il ruolo dell'assegnatario tra
 * {@code UNASSIGNED_USER} e {@code ASSIGNED_USER} in base alle issue attive.
 */
@Service
public class IssueService {

    private final IssueRepository   issueRepository;
    private final UserRepository    userRepository;
    private final ProjectRepository projectRepository;

    /**
     * Costruttore con iniezione delle dipendenze.
     *
     * @param issueRepository   repository per le issue
     * @param userRepository    repository per gli utenti
     * @param projectRepository repository per i progetti
     */
    public IssueService(IssueRepository issueRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository) {
        this.issueRepository   = issueRepository;
        this.userRepository    = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Promuove un utente a {@code ASSIGNED_USER} se era {@code UNASSIGNED_USER}.
     *
     * @param user utente da promuovere
     */
    private void promoteToAssigned(User user) {
        if (user.getRole() == User.Role.UNASSIGNED_USER) {
            user.setRole(User.Role.ASSIGNED_USER);
            userRepository.save(user);
        }
    }

    /**
     * Retrocede un utente a {@code UNASSIGNED_USER} se non ha altre issue attive.
     *
     * @param user utente da retrocedere
     */
    private void demoteIfFree(User user) {
        if (user.getRole() == User.Role.ASSIGNED_USER) {
            boolean hasOtherIssues = issueRepository
                    .existsByAssignedToAndStatusNot(user, Issue.Status.DONE);
            if (!hasOtherIssues) {
                user.setRole(User.Role.UNASSIGNED_USER);
                userRepository.save(user);
            }
        }
    }

    /**
     * Crea una nuova issue in un progetto.
     * Se viene specificato un assegnatario e il creatore è {@code ADMIN},
     * la issue viene messa in {@code IN_PROGRESS} e l'assegnatario promosso.
     *
     * @param projectId     ID del progetto
     * @param title         titolo della issue
     * @param description   descrizione della issue
     * @param type          tipo della issue
     * @param priority      priorità della issue
     * @param creatorEmail  email dell'utente creatore
     * @param assignedToId  ID dell'utente assegnatario (opzionale)
     * @param imageUrl      URL dell'immagine allegata (opzionale)
     * @return la issue creata e salvata
     */
    public Issue createIssue(Long projectId, String title, String description,
                             Issue.IssueType type, Issue.Priority priority,
                             String creatorEmail, Long assignedToId,
                             String imageUrl) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        Issue issue = new Issue();
        issue.setProject(project);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setType(type);
        issue.setPriority(priority);
        issue.setStatus(Issue.Status.TODO);
        issue.setCreatedBy(creator);

        if (imageUrl != null && !imageUrl.isEmpty())
            issue.setImageUrl(imageUrl);

        if (assignedToId != null && creator.getRole() == User.Role.ADMIN) {
            User assignee = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new RuntimeException("Assegnatario non trovato"));
            issue.setAssignedTo(assignee);
            issue.setStatus(Issue.Status.IN_PROGRESS);
            promoteToAssigned(assignee);
        }

        return issueRepository.save(issue);
    }

    /**
     * Restituisce le issue di un progetto filtrate per keyword, tipo, stato e priorità.
     *
     * @param projectId ID del progetto
     * @param keyword   parola chiave per filtrare il titolo (opzionale)
     * @param type      tipo della issue (opzionale)
     * @param status    stato della issue (opzionale)
     * @param priority  priorità della issue (opzionale)
     * @return lista di issue corrispondenti ai filtri
     */
    public List<Issue> getIssues(Long projectId, String keyword,
                                 Issue.IssueType type, Issue.Status status,
                                 Issue.Priority priority) {
        return issueRepository.findWithFilters(projectId, keyword, type, status, priority);
    }

    /**
     * Modifica una issue esistente.
     * Consentito all'assegnatario o a un {@code ADMIN}.
     *
     * @param issueId        ID della issue da modificare
     * @param title          nuovo titolo (opzionale)
     * @param description    nuova descrizione (opzionale)
     * @param status         nuovo stato (opzionale)
     * @param requesterEmail email dell'utente che richiede la modifica
     * @param imageUrl       nuovo URL immagine (opzionale)
     * @param deadlineStr    nuova scadenza in formato {@code yyyy-MM-dd} (opzionale)
     * @return la issue aggiornata
     * @throws RuntimeException se l'utente non ha i permessi
     */
    public Issue updateIssue(Long issueId, String title, String description,
                             Issue.Status status, String requesterEmail,
                             String imageUrl, String deadlineStr) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isAdmin    = requester.getRole() == User.Role.ADMIN;
        boolean isAssigned = issue.getAssignedTo() != null &&
                issue.getAssignedTo().getEmail().equals(requesterEmail);

        if (!isAdmin && !isAssigned)
            throw new RuntimeException("Non hai i permessi per modificare questa issue");

        if (title != null)       issue.setTitle(title);
        if (description != null) issue.setDescription(description);
        if (status != null)      issue.setStatus(status);
        if (imageUrl != null && !imageUrl.isEmpty()) issue.setImageUrl(imageUrl);
        if (deadlineStr != null && !deadlineStr.isEmpty())
            issue.setDeadline(LocalDateTime.parse(deadlineStr + "T00:00:00"));

        return issueRepository.save(issue);
    }

    /**
     * Assegna una issue a un utente.
     * Consentito solo agli {@code ADMIN}.
     * Aggiorna il ruolo del nuovo assegnatario e retrocede il precedente se libero.
     *
     * @param issueId        ID della issue da assegnare
     * @param userId         ID dell'utente a cui assegnare la issue
     * @param requesterEmail email dell'admin che esegue l'assegnazione
     * @return la issue aggiornata
     * @throws RuntimeException se il richiedente non è {@code ADMIN}
     */
    public Issue assignIssue(Long issueId, Long userId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (requester.getRole() != User.Role.ADMIN)
            throw new RuntimeException("Solo gli admin possono assegnare issue");

        User oldAssignee = issue.getAssignedTo();
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        issue.setAssignedTo(assignee);
        issue.setStatus(Issue.Status.IN_PROGRESS);

        if (!issue.getProject().getMembers().contains(assignee)) {
            issue.getProject().getMembers().add(assignee);
            projectRepository.save(issue.getProject());
        }

        Issue saved = issueRepository.save(issue);
        promoteToAssigned(assignee);

        if (oldAssignee != null && !oldAssignee.getId().equals(assignee.getId()))
            demoteIfFree(oldAssignee);

        return saved;
    }

    /**
     * Imposta la scadenza di una issue. Consentito solo agli {@code ADMIN}.
     *
     * @param issueId        ID della issue
     * @param deadline       data e ora della scadenza
     * @param requesterEmail email dell'admin che imposta la scadenza
     * @return la issue aggiornata
     * @throws RuntimeException se il richiedente non è {@code ADMIN}
     */
    public Issue setDeadline(Long issueId, LocalDateTime deadline, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        if (requester.getRole() != User.Role.ADMIN)
            throw new RuntimeException("Solo gli admin possono impostare scadenze");
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        issue.setDeadline(deadline);
        return issueRepository.save(issue);
    }

    /**
     * Restituisce una issue per ID.
     *
     * @param id ID della issue
     * @return la issue trovata
     * @throws RuntimeException se la issue non esiste
     */
    public Issue getIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
    }

    /**
     * Segna una issue come completata ({@code DONE}) e rimuove l'assegnatario.
     * Retrocede l'ex assegnatario se non ha altre issue attive.
     * Consentito all'assegnatario o a un {@code ADMIN}.
     *
     * @param issueId        ID della issue da completare
     * @param requesterEmail email dell'utente che completa la issue
     * @return la issue aggiornata
     * @throws RuntimeException se l'utente non è autorizzato
     */
    public Issue completeIssue(Long issueId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isAdmin    = requester.getRole() == User.Role.ADMIN;
        boolean isAssignee = requester.equals(issue.getAssignedTo());

        if (!isAdmin && !isAssignee)
            throw new RuntimeException("Non autorizzato");

        User wasAssignee = issue.getAssignedTo();
        issue.setStatus(Issue.Status.DONE);
        issue.setAssignedTo(null);
        Issue saved = issueRepository.save(issue);

        if (wasAssignee != null)
            demoteIfFree(wasAssignee);

        return saved;
    }

    /**
     * Elimina una issue. Consentito solo agli {@code ADMIN}
     * e solo se la issue è in stato {@code DONE}.
     *
     * @param issueId        ID della issue da eliminare
     * @param requesterEmail email dell'admin che richiede l'eliminazione
     * @throws SecurityException se l'utente non è {@code ADMIN}
     *                           o la issue non è completata
     */
    public void deleteIssue(Long issueId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (requester.getRole() != User.Role.ADMIN)
            throw new SecurityException("Solo gli admin possono eliminare issue");
        if (issue.getStatus() != Issue.Status.DONE)
            throw new SecurityException("Puoi eliminare solo issue completate");

        issueRepository.delete(issue);
    }
}
