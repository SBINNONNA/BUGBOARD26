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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Project> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(projectService.create(
                body.get("name"),
                body.getOrDefault("description", "")
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id) {
        return projectService.getProjectMembers(id);
    }

    @PostMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                       @PathVariable Long userId) {
        return projectService.addMember(id, userId);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId) {
        return projectService.removeMember(id, userId);
    }
}
