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
@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    // Requisito 2 — Crea issue (con assegnazione opzionale per admin)
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
                body.get("imageUrl")   // ← AGGIUNTO
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }




    // Requisito 3 — Vista issue con filtri
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

    // Singola issue per ID
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssue(@PathVariable Long projectId, @PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    // Requisito 9 — Modifica issue (titolo, descrizione, stato)
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
                body.get("imageUrl"));  // ← AGGIUNTO
        return ResponseEntity.ok(updated);
    }

    // ✅ NUOVO — Assegna issue a un utente (solo admin)
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

    // Requisito 18 — Imposta deadline
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
    @PatchMapping("/{issueId}/complete")
    public ResponseEntity<?> completeIssue(@PathVariable Long projectId,
                                           @PathVariable Long issueId,
                                           Principal principal) {
        return ResponseEntity.ok(issueService.completeIssue(issueId, principal.getName()));
    }

}
