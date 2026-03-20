package com.bugboard.bugboard26.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private List<User> members = new ArrayList<>();

    // Getters & Setters
    public Long getId()                        { return id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public User getCreatedBy()                 { return createdBy; }
    public void setCreatedBy(User u)           { this.createdBy = u; }
    public List<User> getMembers()             { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}
