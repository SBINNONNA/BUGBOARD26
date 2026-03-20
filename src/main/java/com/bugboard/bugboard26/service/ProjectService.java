package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.ProjectRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository    userRepo;

    public ProjectService(ProjectRepository p, UserRepository u) {
        this.projectRepo = p;
        this.userRepo    = u;
    }

    public List<Project> getAll() {
        return projectRepo.findAll();
    }

    public Project create(String name, String description) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow();
        Project p = new Project();
        p.setName(name);
        p.setDescription(description);
        p.setCreatedBy(user);
        return projectRepo.save(p);
    }

    public void delete(Long id) {
        projectRepo.deleteById(id);
    }

    /** Restituisce ADMIN globali + utenti assegnati al progetto, senza duplicati */
    public ResponseEntity<?> getProjectMembers(Long projectId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        List<User> admins = userRepo.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .collect(Collectors.toList());

        List<User> result = Stream.concat(admins.stream(), project.getMembers().stream())
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /** Aggiunge un utente come membro del progetto */
    public ResponseEntity<?> addMember(Long projectId, Long userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            // Aggiorna il ruolo a ASSIGNED_USER se era UNASSIGNED
            if (user.getRole() == User.Role.UNASSIGNED_USER) {
                user.setRole(User.Role.ASSIGNED_USER);
                userRepo.save(user);
            }
            projectRepo.save(project);
        }
        return ResponseEntity.ok("Membro aggiunto");
    }

    /** Rimuove un utente dal progetto */
    public ResponseEntity<?> removeMember(Long projectId, Long userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));
        project.getMembers().removeIf(u -> u.getId().equals(userId));
        projectRepo.save(project);
        return ResponseEntity.ok("Membro rimosso");
    }
}
