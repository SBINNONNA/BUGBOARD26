package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.CommentRepository;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          IssueRepository issueRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long issueId, String text, String authorEmail) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovata"));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Comment comment = new Comment();
        comment.setText(text);
        comment.setIssue(issue);
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}
