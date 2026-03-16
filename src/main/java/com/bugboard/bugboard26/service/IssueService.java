package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public IssueService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    // Requisito 2 — Crea issue
    public Issue createIssue(String title, String description,
                             Issue.IssueType type, Issue.Priority priority,
                             String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setType(type);
        issue.setPriority(priority);
        issue.setStatus(Issue.Status.TODO);
        issue.setCreatedBy(creator);
        return issueRepository.save(issue);
    }

    // Requisito 3 — Filtra issue
    public List<Issue> getIssues(String keyword, Issue.IssueType type,
                                 Issue.Status status, Issue.Priority priority) {
        return issueRepository.findWithFilters(keyword, type, status, priority);
    }

    // Requisito 9 — Modifica issue (con controllo permessi)
    public Issue updateIssue(Long issueId, String title, String description,
                             Issue.Status status, String requesterEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        boolean isAssigned = issue.getAssignedTo() != null &&
                issue.getAssignedTo().getEmail().equals(requesterEmail);

        if (!isAdmin && !isAssigned) {
            throw new RuntimeException("Non hai i permessi per modificare questa issue");
        }

        if (title != null) issue.setTitle(title);
        if (description != null) issue.setDescription(description);
        if (status != null) issue.setStatus(status);
        return issueRepository.save(issue);
    }

    // Requisito 18 — Imposta deadline (solo admin)
    public Issue setDeadline(Long issueId, LocalDateTime deadline, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        if (requester.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Solo gli admin possono impostare scadenze");
        }
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
