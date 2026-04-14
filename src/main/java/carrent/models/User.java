package carrent.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    private String id;
    private String login;
    private String password;
    private Role role;
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