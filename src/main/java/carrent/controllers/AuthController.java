package carrent.controllers;

import carrent.dto.LoginRequest;
import carrent.dto.LoginResponse;
import carrent.models.User;
import carrent.repositories.UserRepository;
import carrent.security.JwtUtil;
import carrent.services.inter.AuthServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthServiceInterface authService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          AuthServiceInterface authService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.login(),
                        loginRequest.password()
                )
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.login());
        final String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        boolean success = authService.register(
                newUser.getLogin(),
                newUser.getPassword(),
                newUser.getRole().name()
        );

        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rejestracja nie powiodła się."));
        }
        return ResponseEntity.ok(Map.of("message", "Użytkownik zarejestrowany pomyślnie."));
    }
}