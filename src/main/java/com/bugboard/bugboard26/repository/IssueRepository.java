package com.bugboard.bugboard26.repository;

import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.Issue.IssueType;
import com.bugboard.bugboard26.model.Issue.Priority;
import com.bugboard.bugboard26.model.Issue.Status;
import com.bugboard.bugboard26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Query("SELECT i FROM Issue i WHERE " +
            "i.project.id = :projectId " +
            "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:type IS NULL OR i.type = :type) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:priority IS NULL OR i.priority = :priority)")
    List<Issue> findWithFilters(
            @Param("projectId") Long projectId,
            @Param("keyword")   String keyword,
            @Param("type")      IssueType type,
            @Param("status")    Status status,
            @Param("priority")  Priority priority
    );
    boolean existsByAssignedToAndStatusNot(User assignedTo, Issue.Status status);


}
