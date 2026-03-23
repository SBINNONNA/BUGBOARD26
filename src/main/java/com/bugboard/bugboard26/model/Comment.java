package com.bugboard.bugboard26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entità che rappresenta un commento associato a una issue.
 * <p>
 * Ogni commento contiene il testo, la data di creazione, il riferimento
 * alla issue a cui appartiene e l'autore che lo ha scritto.
 * </p>
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ← AGGIUNTO
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Restituisce l'identificativo del commento.
     *
     * @return id del commento
     */
    public Long getId() { return id; }

    /**
     * Imposta l'identificativo del commento.
     *
     * @param id identificativo da assegnare
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Restituisce il testo del commento.
     *
     * @return testo del commento
     */
    public String getText() { return text; }

    /**
     * Imposta il testo del commento.
     *
     * @param text testo del commento
     */
    public void setText(String text) { this.text = text; }

    /**
     * Restituisce la data e ora di creazione del commento.
     *
     * @return data di creazione
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Imposta la data e ora di creazione del commento.
     *
     * @param createdAt data e ora da assegnare
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Restituisce la issue a cui il commento è associato.
     *
     * @return issue collegata
     */
    public Issue getIssue() { return issue; }

    /**
     * Imposta la issue a cui il commento appartiene.
     *
     * @param issue issue collegata
     */
    public void setIssue(Issue issue) { this.issue = issue; }

    /**
     * Restituisce l'autore del commento.
     *
     * @return utente autore
     */
    public User getAuthor() { return author; }

    /**
     * Imposta l'autore del commento.
     *
     * @param author utente autore
     */
    public void setAuthor(User author) { this.author = author; }
}