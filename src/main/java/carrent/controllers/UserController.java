package carrent.controllers;

import carrent.models.User;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
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
    public List<User> list() {
        return authService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable String id) {
        return authService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie odnaleziono użytkownika o podanym ID: " + id));
    }
}