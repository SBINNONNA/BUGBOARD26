package com.bugboard.bugboard26.controller;

import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        if (userRepo.findByEmail(body.get("email")).isPresent())
            return ResponseEntity.badRequest().body("Email già in uso");

        User.Role role = User.Role.UNASSIGNED_USER; // default
        if (body.containsKey("role")) {
            try {
                role = User.Role.valueOf(body.get("role").toUpperCase());
                if (role == User.Role.ASSIGNED_USER)  // ← bloccato: lo assegna il progetto
                    return ResponseEntity.badRequest().body("Ruolo non valido per la creazione");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Ruolo non riconosciuto");
            }
        }

        User u = new User();
        u.setEmail(body.get("email"));
        u.setPassword(passwordEncoder.encode(body.get("password")));
        u.setRole(role);

        return ResponseEntity.status(HttpStatus.CREATED).body(userRepo.save(u));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("role"))  user.setRole(User.Role.valueOf(body.get("role")));
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
