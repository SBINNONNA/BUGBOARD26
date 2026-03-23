package com.bugboard.bugboard26.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta un progetto del sistema.
 * <p>
 * Un progetto contiene issue e può avere più membri assegnati.
 * Ogni progetto ha un nome univoco, una descrizione opzionale,
 * la data di creazione e il suo creatore.
 * </p>
 */
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

    /**
     * Restituisce l'identificativo del progetto.
     *
     * @return id del progetto
     */
    public Long getId() { return id; }

    /**
     * Restituisce il nome del progetto.
     *
     * @return nome del progetto
     */
    public String getName() { return name; }

    /**
     * Imposta il nome del progetto.
     *
     * @param name nome del progetto
     */
    public void setName(String name) { this.name = name; }

    /**
     * Restituisce la descrizione del progetto.
     *
     * @return descrizione del progetto
     */
    public String getDescription() { return description; }

    /**
     * Imposta la descrizione del progetto.
     *
     * @param d descrizione del progetto
     */
    public void setDescription(String d) { this.description = d; }

    /**
     * Restituisce la data di creazione del progetto.
     *
     * @return data di creazione
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Restituisce l'utente che ha creato il progetto.
     *
     * @return creatore del progetto
     */
    public User getCreatedBy() { return createdBy; }

    /**
     * Imposta l'utente che ha creato il progetto.
     *
     * @param u creatore del progetto
     */
    public void setCreatedBy(User u) { this.createdBy = u; }

    /**
     * Restituisce la lista dei membri del progetto.
     *
     * @return lista degli utenti membri
     */
    public List<User> getMembers() { return members; }

    /**
     * Imposta la lista dei membri del progetto.
     *
     * @param members lista degli utenti membri
     */
    public void setMembers(List<User> members) { this.members = members; }
}