package carrent.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "password", nullable = true)
    private String password;

    @Transient
    private String rentedVehicleId;

    public User copy() {
        return User.builder()
                .id(id)
                .login(login)
                .password(password)
                .passwordHash(passwordHash)
                .role(role)
                .rentedVehicleId(rentedVehicleId)
                .build();
    }

    @Override
    public String toString() {
        return "Użytkownik: " + login + " [" + role + "], ID: " + id +
                ", Wypożyczony pojazd ID: " +
                (rentedVehicleId == null || rentedVehicleId.isEmpty() ? "Brak" : rentedVehicleId);
    }
}