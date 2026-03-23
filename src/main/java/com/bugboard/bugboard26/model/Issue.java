package com.bugboard.bugboard26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entità che rappresenta una issue del sistema.
 * <p>
 * Una issue appartiene a un progetto e può essere assegnata a un utente.
 * Contiene informazioni su titolo, descrizione, tipo, priorità, stato,
 * scadenza, immagine allegata e autore.
 * </p>
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    private IssueType type;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    private LocalDateTime deadline;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Tipologia della issue.
     */
    public enum IssueType { BUG, QUESTION, FEATURE, DOCUMENTATION }

    /**
     * Priorità della issue.
     */
    public enum Priority  { P1, P2, P3, P4, P5 }

    /**
     * Stato di avanzamento della issue.
     */
    public enum Status    { TODO, IN_PROGRESS, DONE }

    /**
     * Restituisce l'identificativo della issue.
     *
     * @return id della issue
     */
    public Long getId() { return id; }

    /**
     * Imposta l'identificativo della issue.
     *
     * @param id identificativo da assegnare
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Restituisce il titolo della issue.
     *
     * @return titolo della issue
     */
    public String getTitle() { return title; }

    /**
     * Imposta il titolo della issue.
     *
     * @param title titolo della issue
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Restituisce la descrizione della issue.
     *
     * @return descrizione della issue
     */
    public String getDescription() { return description; }

    /**
     * Imposta la descrizione della issue.
     *
     * @param description descrizione della issue
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Restituisce il tipo della issue.
     *
     * @return tipo della issue
     */
    public IssueType getType() { return type; }

    /**
     * Imposta il tipo della issue.
     *
     * @param type tipo della issue
     */
    public void setType(IssueType type) { this.type = type; }

    /**
     * Restituisce la priorità della issue.
     *
     * @return priorità della issue
     */
    public Priority getPriority() { return priority; }

    /**
     * Imposta la priorità della issue.
     *
     * @param priority priorità della issue
     */
    public void setPriority(Priority priority) { this.priority = priority; }

    /**
     * Restituisce lo stato della issue.
     *
     * @return stato della issue
     */
    public Status getStatus() { return status; }

    /**
     * Imposta lo stato della issue.
     *
     * @param status stato della issue
     */
    public void setStatus(Status status) { this.status = status; }

    /**
     * Restituisce la data di scadenza della issue.
     *
     * @return deadline della issue
     */
    public LocalDateTime getDeadline() { return deadline; }

    /**
     * Imposta la data di scadenza della issue.
     *
     * @param deadline data di scadenza
     */
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    /**
     * Restituisce l'URL dell'immagine allegata.
     *
     * @return URL dell'immagine
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Imposta l'URL dell'immagine allegata.
     *
     * @param imageUrl URL dell'immagine
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Restituisce l'utente che ha creato la issue.
     *
     * @return autore della issue
     */
    public User getCreatedBy() { return createdBy; }

    /**
     * Imposta l'utente che ha creato la issue.
     *
     * @param createdBy autore della issue
     */
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    /**
     * Restituisce l'utente assegnato alla issue.
     *
     * @return assegnatario della issue
     */
    public User getAssignedTo() { return assignedTo; }

    /**
     * Imposta l'utente assegnato alla issue.
     *
     * @param assignedTo assegnatario della issue
     */
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    /**
     * Restituisce il progetto a cui la issue appartiene.
     *
     * @return progetto della issue
     */
    public Project getProject() { return project; }

    /**
     * Imposta il progetto a cui la issue appartiene.
     *
     * @param project progetto della issue
     */
    public void setProject(Project project) { this.project = project; }
}