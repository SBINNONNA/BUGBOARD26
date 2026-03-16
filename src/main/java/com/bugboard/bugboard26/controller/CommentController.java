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

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Requisito 5 — Aggiungi commento
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long issueId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Comment comment = commentService.addComment(
                issueId, body.get("text"), userDetails.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // Requisito 5 — Visualizza commenti
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long issueId) {
        return ResponseEntity.ok(commentService.getCommentsByIssue(issueId));
    }
}
