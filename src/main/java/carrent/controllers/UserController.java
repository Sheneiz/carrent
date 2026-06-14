package carrent.controllers;

import carrent.models.User;
import org.springframework.http.ResponseEntity; // Dodany import dla ResponseEntity
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final carrent.services.inter.AuthServiceInterface authService;

    public UserController(carrent.services.inter.AuthServiceInterface authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        return authService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Nie odnaleziono użytkownika o podanym ID: " + id));
    }
}