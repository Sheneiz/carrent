package carrent;

import carrent.models.User;
import carrent.services.AuthService;
import carrent.services.RentalService;
import carrent.services.VehicleService;

import java.util.Scanner;

public class App {
    private final AuthService authService;
    private final VehicleService vehicleService;
    private final RentalService rentalService;
    private final Scanner scanner = new Scanner(System.in);

    private User currentUser = null;
    private boolean running = true;

    public App(AuthService authService, VehicleService vehicleService, RentalService rentalService) {
        this.authService = authService;
        this.vehicleService = vehicleService;
        this.rentalService = rentalService;
    }

    public void run() {
        System.out.println("--- System Wypożyczalni Pojazdów ---");

        while (running) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n1. Zaloguj się\n2. Zarejestruj się\n3. Wyjdź");
        System.out.print("Wybór: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> handleRegistration();
            case "3" -> running = false;
        }
    }

    private void handleLogin() {
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();

        authService.login(login, password).ifPresentOrElse(
                user -> this.currentUser = user,
                () -> System.out.println("Błąd: Niepoprawny login lub hasło.")
        );
    }

    private void handleRegistration() {
        System.out.println("\n--- Rejestracja ---");
        System.out.print("Nowy login: ");
        String login = scanner.nextLine();
        System.out.print("Nowe hasło: ");
        String password = scanner.nextLine();

        if (authService.register(login, password, "USER")) {
            System.out.println("Rejestracja pomyślna! Możesz się teraz zalogować.");
        } else {
            System.out.println("Błąd: Login jest już zajęty.");
        }
    }

    private void showMainMenu() {
        System.out.println("\nZalogowano jako: " + currentUser.getLogin() + " [" + currentUser.getRole() + "]");
        System.out.println("1. Lista dostępnych pojazdów");
        System.out.println("2. Wypożycz pojazd");
        System.out.println("3. Zwróć pojazd");

        if (currentUser.getRole().toString().equals("ADMIN")) {
            System.out.println("9. Usuń pojazd (ADMIN)");
        }

        System.out.println("0. Wyloguj");

        System.out.print("Wybierz: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> displayAvailableVehicles();
            case "2" -> handleRent();
            case "3" -> handleReturn();
            case "9" -> {
                if (currentUser.getRole().toString().equals("ADMIN")) handleAdminDelete();
            }
            case "0" -> currentUser = null;
            default -> System.out.println("Niepoprawny wybór.");
        }
    }

    private void displayAvailableVehicles() {
        System.out.println("\n--- Dostępne pojazdy ---");
        vehicleService.getAvailableVehicles().forEach(System.out::println);
    }

    private void handleRent() {
        System.out.print("Podaj ID pojazdu: ");
        String vehicleId = scanner.nextLine();

        if (rentalService.rentVehicle(currentUser.getId(), vehicleId)) {
            System.out.println("Pojazd został pomyślnie wypożyczony.");
        } else {
            System.out.println("Błąd: Pojazd jest już zajęty.");
        }
    }

    private void handleReturn() {
        System.out.print("Podaj ID pojazdu do zwrotu: ");
        String vehicleId = scanner.nextLine();

        if (rentalService.returnVehicle(vehicleId)) {
            System.out.println("Pojazd został pomyślnie zwrócony.");
        } else {
            System.out.println("Błąd: Nie znaleziono aktywnego wypożyczenia.");
        }
    }

    private void handleAdminDelete() {
        System.out.print("Podaj ID pojazdu do usunięcia: ");
        String id = scanner.nextLine();
    }
}