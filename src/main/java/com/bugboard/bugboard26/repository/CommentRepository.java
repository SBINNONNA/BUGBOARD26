package com.bugboard.bugboard26.repository;

import com.bugboard.bugboard26.model.Comment;
import com.bugboard.bugboard26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Tutti i commenti di una issue, in ordine cronologico (requisito 5)
    List<Comment> findByIssueIdOrderByCreatedAtAsc(Long issueId);
    List<Comment> findByAuthor(User author);

}
