package carrent;

public class User {
    private String login;
    private String password;
    private Role role;
    private String rentedVehicleId;

    public User(String login, String password, Role role, String rentedVehicleId) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.rentedVehicleId = rentedVehicleId;
    }

    public User(User other) {
        this.login = other.login;
        this.password = other.password;
        this.role = other.role;
        this.rentedVehicleId = other.rentedVehicleId;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getRentedVehicleId() { return rentedVehicleId; }
    public void setRentedVehicleId(String rentedVehicleId) { this.rentedVehicleId = rentedVehicleId; }

    @Override
    public String toString() {
        return "Użytkownik: " + login + " [" + role + "], Wypożyczony pojazd ID: " +
                (rentedVehicleId == null || rentedVehicleId.isEmpty() ? "Brak" : rentedVehicleId);
    }
}