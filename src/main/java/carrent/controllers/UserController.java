package carrent.controllers;

import carrent.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final carrent.services.inter.AuthServiceInterface authService;

    public UserController(carrent.services.inter.AuthServiceInterface authService) {
        this.authService = authService;
    }
    public record UserResponse(Object id, String login, String role) {}

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> responses = authService.getAllUsers().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getLogin(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable String id) {
        return authService.getUserById(id)
                .map(user -> ResponseEntity.ok(new UserResponse(
                        user.getId(),
                        user.getLogin(),
                        user.getRole().name()
                )))
                .orElseThrow(() -> new IllegalArgumentException("Nie odnaleziono użytkownika o podanym ID: " + id));
    }
}