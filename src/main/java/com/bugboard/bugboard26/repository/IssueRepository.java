
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

/**
 * Repository Spring Data JPA per la gestione delle issue.
 * <p>
 * Fornisce metodi per l'accesso ai dati delle issue, inclusi
 * metodi personalizzati per la ricerca con filtri multipli
 * e controllo dell'esistenza di issue assegnate.
 * </p>
 */
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Trova le issue di un progetto specifico applicando filtri opzionali.
     * <p>
     * Esegue una ricerca filtrata su:
     * <ul>
     *   <li>Progetto (obbligatorio)</li>
     *   <li>Parola chiave nel titolo o descrizione (opzionale)</li>
     *   <li>Tipo di issue (opzionale)</li>
     *   <li>Stato (opzionale)</li>
     *   <li>Priorità (opzionale)</li>
     * </ul>
     * </p>
     * <p>
     * La ricerca per parola chiave è case-insensitive e utilizza
     * l'operatore LIKE per cercare sottostringhe.
     * </p>
     *
     * @param projectId identificativo del progetto (obbligatorio)
     * @param keyword   termine di ricerca per titolo/descrizione (può essere null)
     * @param type      tipo di issue da filtrare (può essere null)
     * @param status    stato delle issue da filtrare (può essere null)
     * @param priority  priorità delle issue da filtrare (può essere null)
     * @return lista delle issue che soddisfano i criteri di ricerca
     */
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

    /**
     * Verifica se esistono issue assegnate a un utente con stato diverso da quello specificato.
     * <p>
     * Utile per controllare se un utente ha issue attive (non completate)
     * prima di eseguire operazioni come l'eliminazione o la modifica del ruolo.
     * </p>
     *
     * @param assignedTo utente assegnatario delle issue
     * @param status     stato da escludere dal controllo
     * @return true se esistono issue assegnate con stato diverso da quello specificato
     */
    boolean existsByAssignedToAndStatusNot(User assignedTo, Issue.Status status);
}