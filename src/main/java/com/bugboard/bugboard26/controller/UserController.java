package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("role"))  user.setRole(User.Role.valueOf(body.get("role"))); // String diretto
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
