package com.bugboard.bugboard26.service;

import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.ProjectRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
