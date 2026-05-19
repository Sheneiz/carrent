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
@ToString
public class User {
    @Id
    @Column(nullable = false, unique = true)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    @Column(nullable = false)
    private String password;
    
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