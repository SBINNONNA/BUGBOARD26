package com.bugboard.bugboard26.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Entità che rappresenta un utente del sistema.
 * <p>
 * Ogni utente ha un'email univoca, una password cifrata, un ruolo
 * e può avere una foto profilo opzionale. Gli utenti possono
 * essere assegnati a progetti e issue in base al loro ruolo.
 * </p>
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
public class User {
    @Column(name = "profile_picture")
    private String profilePicture;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Ruoli disponibili per gli utenti del sistema.
     */
    public enum Role {
        /** Amministratore: gestione completa del sistema */
        ADMIN,
        /** Utente assegnato: lavora sulle issue assegnate */
        ASSIGNED_USER,
        /** Utente non assegnato: può solo commentare */
        UNASSIGNED_USER
    }

    /**
     * Restituisce l'identificativo dell'utente.
     *
     * @return id dell'utente
     */
    public Long getId() { return id; }

    /**
     * Imposta l'identificativo dell'utente.
     *
     * @param id identificativo da assegnare
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Restituisce l'email dell'utente.
     *
     * @return email dell'utente
     */
    public String getEmail() { return email; }

    /**
     * Imposta l'email dell'utente.
     *
     * @param email email dell'utente
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Restituisce la password cifrata dell'utente.
     *
     * @return password cifrata
     */
    public String getPassword() { return password; }

    /**
     * Imposta la password cifrata dell'utente.
     *
     * @param password password cifrata
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Restituisce il ruolo dell'utente.
     *
     * @return ruolo dell'utente
     */
    public Role getRole() { return role; }

    /**
     * Imposta il ruolo dell'utente.
     *
     * @param role ruolo da assegnare
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * Restituisce l'URL della foto profilo dell'utente.
     *
     * @return URL della foto profilo
     */
    public String getProfilePicture() { return profilePicture; }

    /**
     * Imposta l'URL della foto profilo dell'utente.
     *
     * @param profilePicture URL della foto profilo
     */
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}