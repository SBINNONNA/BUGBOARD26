package com.bugboard.bugboard26.repository;

import com.bugboard.bugboard26.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA per la gestione dei progetti.
 * <p>
 * Fornisce l'accesso ai dati dei progetti utilizzando i metodi CRUD
 * standard forniti da JpaRepository. Al momento non sono definiti
 * metodi di query personalizzati, ma eredita tutte le operazioni
 * base come save, findById, findAll, delete, ecc.
 * </p>
 * <p>
 * L'annotazione {@code @Repository} è opzionale quando si estende
 * JpaRepository, ma è stata mantenuta per chiarezza semantica.
 * </p>
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Nessun metodo personalizzato definito.
    // Utilizza i metodi standard di JpaRepository:
    // - save(Project project)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // - existsById(Long id)
    // - count()
}