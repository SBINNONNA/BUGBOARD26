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

    private final IssueRepository issueRepository;
    private final UserRepository  userRepository;
    private final ProjectRepository projectRepository;  // ← AGGIUNTO

    public IssueService(IssueRepository issueRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository) {
        this.issueRepository   = issueRepository;
        this.userRepository    = userRepository;
        this.projectRepository = projectRepository;
    }

    // Requisito 2 — Crea issue nel progetto corretto
    public Issue createIssue(Long projectId, String title, String description,
                             Issue.IssueType type, Issue.Priority priority,
                             String creatorEmail, Long assignedToId) {   // ← aggiunto assignedToId
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

        // Solo l'admin può assegnare al momento della creazione
        if (assignedToId != null && creator.getRole() == User.Role.ADMIN) {
            User assignee = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new RuntimeException("Assegnatario non trovato"));
            issue.setAssignedTo(assignee);
            issue.setStatus(Issue.Status.IN_PROGRESS);
        }

        return issueRepository.save(issue);
    }



    // Requisito 3 — Filtra issue per progetto
    public List<Issue> getIssues(Long projectId, String keyword,
                                 Issue.IssueType type, Issue.Status status,
                                 Issue.Priority priority) {
        return issueRepository.findWithFilters(projectId, keyword, type, status, priority);
    }

    // Requisito 9 — Modifica issue
    // Requisito 9 — Modifica issue (aggiornato con auto-status)
    public Issue updateIssue(Long issueId, String title, String description,
                             Issue.Status status, String requesterEmail) {
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

        // ✅ Auto-status: se ha un assegnatario e non è DONE → IN_PROGRESS
        if (issue.getAssignedTo() != null && issue.getStatus() != Issue.Status.DONE) {
            issue.setStatus(Issue.Status.IN_PROGRESS);
        }

        return issueRepository.save(issue);
    }

    // ✅ NUOVO — Assegna issue a un utente
    public Issue assignIssue(Long issueId, Long userId, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (requester.getRole() != User.Role.ADMIN)
            throw new RuntimeException("Solo gli admin possono assegnare issue");

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        issue.setAssignedTo(assignee);
        issue.setStatus(Issue.Status.IN_PROGRESS); // ← automatico
        return issueRepository.save(issue);
    }


    // Requisito 18 — Imposta deadline (solo admin)
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
}
