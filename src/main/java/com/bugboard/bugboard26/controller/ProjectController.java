package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService s) { this.projectService = s; }

    @GetMapping
    public List<Project> getAll() {
        return projectService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Project> create(@RequestBody Map<String, String> body) {
        Project p = projectService.create(
                body.get("name"),
                body.getOrDefault("description", "")
        );
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
