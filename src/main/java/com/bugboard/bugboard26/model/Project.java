package com.bugboard.bugboard26.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    private User createdBy;

    // Getters & Setters
    public Long getId()                    { return id; }
    public String getName()                { return name; }
    public void setName(String name)       { this.name = name; }
    public String getDescription()         { return description; }
    public void setDescription(String d)   { this.description = d; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public User getCreatedBy()             { return createdBy; }
    public void setCreatedBy(User u)       { this.createdBy = u; }
}
