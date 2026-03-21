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

@Service
public class IssueService {

    private final IssueRepository   issueRepository;
    private final UserRepository    userRepository;
    private final ProjectRepository projectRepository;

    public IssueService(IssueRepository issueRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository) {
        this.issueRepository   = issueRepository;
        this.userRepository    = userRepository;
        this.projectRepository = projectRepository;
    }

    // ── UTILITY: promuovi utente ad ASSIGNED_USER ──────────
    private void promoteToAssigned(User user) {
        if (user.getRole() == User.Role.UNASSIGNED_USER) {
            user.setRole(User.Role.ASSIGNED_USER);
            userRepository.save(user);
        }
    }

    // ── UTILITY: retrocedi se non ha altre issue attive ────
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

    // ── Crea issue ─────────────────────────────────────────
    public Issue createIssue(Long projectId, String title, String description,
                             Issue.IssueType type, Issue.Priority priority,
                             String creatorEmail, Long assignedToId,
                             String imageUrl) {   // ← AGGIUNTO
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

        // ← AGGIUNTO
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

    // ── Filtra issue ───────────────────────────────────────
    public List<Issue> getIssues(Long projectId, String keyword,
                                 Issue.IssueType type, Issue.Status status,
                                 Issue.Priority priority) {
        return issueRepository.findWithFilters(projectId, keyword, type, status, priority);
    }

    // ── Modifica issue ─────────────────────────────────────
    public Issue updateIssue(Long issueId, String title, String description,
                             Issue.Status status, String requesterEmail,
                             String imageUrl, String deadlineStr) { // ← aggiunto deadlineStr
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

        // ← salva la deadline se presente
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            issue.setDeadline(LocalDateTime.parse(deadlineStr + "T00:00:00"));
        }

        return issueRepository.save(issue);
    }

    // ── Assegna issue ──────────────────────────────────────
    public Issue assignIssue(Long issueId, Long userId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (requester.getRole() != User.Role.ADMIN)
            throw new RuntimeException("Solo gli admin possono assegnare issue");

        // ── Se c'era già un assegnatario, potrebbe diventare libero ──
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

        promoteToAssigned(assignee);  // ← promuovi nuovo assegnatario

        // ── Retrocedi il vecchio solo se era diverso ──
        if (oldAssignee != null && !oldAssignee.getId().equals(assignee.getId())) {
            demoteIfFree(oldAssignee);
        }

        return saved;
    }

    // ── Deadline ───────────────────────────────────────────
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

    public Issue getIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
    }

    // ── Completa issue ─────────────────────────────────────
    public Issue completeIssue(Long issueId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isAdmin    = requester.getRole() == User.Role.ADMIN;
        boolean isAssignee = requester.equals(issue.getAssignedTo());

        if (!isAdmin && !isAssignee)
            throw new RuntimeException("Non autorizzato");

        User wasAssignee = issue.getAssignedTo(); // ← salva prima di nullare

        issue.setStatus(Issue.Status.DONE);
        issue.setAssignedTo(null);
        Issue saved = issueRepository.save(issue);

        if (wasAssignee != null) {
            demoteIfFree(wasAssignee); // ← retrocedi se libero
        }

        return saved;
    }
}
