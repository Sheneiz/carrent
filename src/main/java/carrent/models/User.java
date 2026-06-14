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

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

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
}