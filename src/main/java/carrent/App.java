package carrent;

import carrent.models.Role;
import carrent.models.User;
import carrent.models.Vehicle;
import carrent.services.AuthService;
import carrent.services.RentalService;
import carrent.services.VehicleService;

import java.util.List;
import java.util.Scanner;

public class App {
    private final AuthService authService;
    private final VehicleService vehicleService;
    private final RentalService rentalService;
    private final Scanner scanner = new Scanner(System.in);

    public App(AuthService authService, VehicleService vehicleService, RentalService rentalService) {
        this.authService = authService;
        this.vehicleService = vehicleService;
        this.rentalService = rentalService;
    }

    public void run() {
        System.out.println("--- System Wypożyczalni Pojazdów ---");

        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();

        authService.login(login, password).ifPresentOrElse(
                this::showMainMenu,
                () -> System.out.println("Błąd logowania: Niepoprawny login lub hasło.")
        );
    }

    private void showMainMenu(User user) {
        boolean running = true;
        while (running) {
            System.out.println("\nZalogowano jako: " + user.getLogin() + " [" + user.getRole() + "]");
            System.out.println("1. Wyświetl dostępne pojazdy");
            System.out.println("2. Wypożycz pojazd");
            System.out.println("3. Zwróć pojazd");
            System.out.println("4. Wyloguj");

            if (user.getRole() == Role.ADMIN) {
                System.out.println("5. Panel Admina (Wszystkie pojazdy)");
                System.out.println("6. Panel Admina (Zarejestruj użytkownika)");
            }

            System.out.print("Wybierz opcję: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> displayAvailableVehicles();
                case "2" -> handleRent(user.getId());
                case "3" -> handleReturn();
                case "4" -> running = false;
                case "5" -> {
                    if (user.getRole() == Role.ADMIN) displayAllVehicles();
                    else System.out.println("Brak uprawnień.");
                }
                case "6" -> {
                    if (user.getRole() == Role.ADMIN) handleRegistration();
                    else System.out.println("Brak uprawnień.");
                }
                default -> System.out.println("Niepoprawna opcja.");
            }
        }
    }

    private void displayAvailableVehicles() {
        System.out.println("\n--- Dostępne pojazdy ---");
        List<Vehicle> available = vehicleService.getAvailableVehicles();
        if (available.isEmpty()) {
            System.out.println("Brak dostępnych pojazdów.");
        } else {
            available.forEach(System.out::println);
        }
    }

    private void handleRent(String userId) {
        System.out.print("Podaj ID pojazdu do wypożyczenia: ");
        String vehicleId = scanner.nextLine();

        if (rentalService.rentVehicle(userId, vehicleId)) {
            System.out.println("Pojazd wypożyczony pomyślnie!");
        } else {
            System.out.println("Nie można wypożyczyć pojazdu (może jest już zajęty).");
        }
    }

    private void handleReturn() {
        System.out.print("Podaj ID pojazdu do zwrotu: ");
        String vehicleId = scanner.nextLine();

        if (rentalService.returnVehicle(vehicleId)) {
            System.out.println("Pojazd został zwrócony.");
        } else {
            System.out.println("Błąd: Nie znaleziono aktywnego wypożyczenia dla tego pojazdu.");
        }
    }

    private void displayAllVehicles() {
        System.out.println("\n--- Wszystkie pojazdy w systemie ---");
        vehicleService.getAvailableVehicles().forEach(System.out::println);
    }

    private void handleRegistration() {
        System.out.println("\n--- Rejestracja Nowego Użytkownika ---");
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();
        System.out.print("Rola (ADMIN/USER): ");
        String role = scanner.nextLine();

        if (authService.register(login, password, role)) {
            System.out.println("Sukces: Użytkownik " + login + " został zarejestrowany.");
        } else {
            System.out.println("Błąd: Login zajęty lub niepoprawna rola.");
        }
    }
}