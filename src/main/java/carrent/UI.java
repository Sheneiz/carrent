package carrent;

import carrent.models.User;
import carrent.services.AuthService;
import carrent.services.RentalService;
import carrent.services.VehicleService;

import java.util.Scanner;

public class UI {
    private final AuthService authService;
    private final VehicleService vehicleService;
    private final RentalService rentalService;
    private final Scanner scanner = new Scanner(System.in);

    private User currentUser = null;
    private boolean running = true;

    public UI(AuthService authService, VehicleService vehicleService, RentalService rentalService) {
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
        System.out.println("\n========================================");
        System.out.println("Zalogowano jako: " + currentUser.getLogin() + " [" + currentUser.getRole() + "]");
        System.out.println("1. Lista dostępnych pojazdów");
        System.out.println("2. Wypożycz pojazd");
        System.out.println("3. Zwróć pojazd");
        System.out.println("4. Moje wypożyczone pojazdy");

        boolean isAdmin = currentUser.getRole().toString().equals("ADMIN");

        if (isAdmin) {
            System.out.println("\n--- PANEL ADMINISTRATORA ---");
            System.out.println("8. Dodaj pojazd");
            System.out.println("9. Usuń pojazd");
            System.out.println("10. Usuń użytkownika");
            System.out.println("11. Wyświetl wszystkie aktywne wypożyczenia");
        }

        System.out.println("\n0. Wyloguj");
        System.out.println("========================================");

        System.out.print("Wybierz: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> displayAvailableVehicles();
            case "2" -> handleRent();
            case "3" -> handleReturn();
            case "4" -> displayRentedVehicles();
            case "8" -> {
                if (isAdmin) handleAdminAdd();
                else System.out.println("Brak uprawnień.");
            }
            case "9" -> {
                if (isAdmin) handleAdminDelete();
                else System.out.println("Brak uprawnień.");
            }
            case "10" -> {
                if (isAdmin) handleAdminDeleteUser();
                else System.out.println("Brak uprawnień.");
            }
            case "11" -> {
                if (isAdmin) handleShowAllRentedVehicles();
                else System.out.println("Brak uprawnień.");
            }
            case "0" -> {
                currentUser = null;
                System.out.println("Wylogowano pomyślnie.");
            }
            default -> System.out.println("Niepoprawny wybór. Spróbuj ponownie.");
        }
    }

    private void displayAvailableVehicles() {
        System.out.println("\n--- Dostępne pojazdy ---");
        java.util.List<carrent.models.Vehicle> available = vehicleService.getAvailableVehicles();
        if (available.isEmpty()) {
            System.out.println("Obecnie brak dostępnych pojazdów.");
            return;
        }
        for (int i = 0; i < available.size(); i++) {
            System.out.println((i + 1) + ". " + available.get(i).toString());
        }
    }

    private void displayRentedVehicles() {
        System.out.println("\n--- Moje wypożyczone pojazdy ---");
        java.util.List<carrent.models.Vehicle> rented = rentalService.getRentedVehicles(currentUser.getId());
        if (rented.isEmpty()) {
            System.out.println("Nie masz aktualnie wypożyczonych pojazdów.");
            return;
        }
        for (int i = 0; i < rented.size(); i++) {
            System.out.println((i + 1) + ". " + rented.get(i).toString());
        }
    }

    private void handleRent() {
        System.out.print("Podaj ID pojazdu (lub jego numer z listy): ");
        String input = scanner.nextLine();
        String vehicleId = input;

        try {
            int index = Integer.parseInt(input) - 1;
            java.util.List<carrent.models.Vehicle> available = vehicleService.getAvailableVehicles();
            if (index >= 0 && index < available.size()) {
                vehicleId = available.get(index).getId();
            }
        } catch (NumberFormatException ignored) {
        }

        if (rentalService.rentVehicle(currentUser.getId(), vehicleId)) {
            System.out.println("Pojazd został pomyślnie wypożyczony.");
        } else {
            System.out.println("Błąd: Pojazd jest już zajęty lub nie istnieje.");
        }
    }

    private void handleReturn() {
        displayRentedVehicles();
        java.util.List<carrent.models.Vehicle> rented = rentalService.getRentedVehicles(currentUser.getId());
        if (rented.isEmpty()) return;

        System.out.print("Podaj ID pojazdu do zwrotu (lub jego numer z listy powyzęj): ");
        String input = scanner.nextLine();
        String vehicleId = input;

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < rented.size()) {
                vehicleId = rented.get(index).getId();
            }
        } catch (NumberFormatException ignored) {
        }

        if (rentalService.returnVehicle(vehicleId)) {
            System.out.println("Pojazd został pomyślnie zwrócony.");
        } else {
            System.out.println("Błąd: Nie znaleziono aktywnego wypożyczenia dla wybranego ID.");
        }
    }

    private void handleAdminDelete() {
        displayAvailableVehicles();
        java.util.List<carrent.models.Vehicle> available = vehicleService.getAvailableVehicles();
        if (available.isEmpty()) return;

        System.out.print("Podaj ID pojazdu do usunięcia (lub jego numer z listy powyżej): ");
        String input = scanner.nextLine();
        String vehicleId = input;

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < available.size()) {
                vehicleId = available.get(index).getId();
            }
        } catch (NumberFormatException ignored) {
        }

        try {
            vehicleService.deleteVehicle(vehicleId);
            System.out.println("Pojazd pomyślnie usunięty.");
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private void handleAdminDeleteUser() {
            System.out.println("\n--- USUWANIE UŻYTKOWNIKA ---");
            java.util.List<carrent.models.User> users = authService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("Brak użytkowników w systemie.");
                return;
            }

            System.out.println("Dostępni użytkownicy:");
            users.forEach(u -> System.out.printf("[%s] - %s (%s)%n", u.getId(), u.getLogin(), u.getRole()));

            System.out.print("\nPodaj ID użytkownika do usunięcia (lub wpisz '0' aby anulować): ");
            String userId = scanner.nextLine().trim();

            if (userId.equals("0") || userId.isEmpty()) {
                System.out.println("Anulowano usuwanie.");
                return;
            }

            try {
                authService.deleteUser(userId);
                System.out.println("Sukces: Użytkownik o ID " + userId + " został usunięty.");
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Błąd: Nie znaleziono użytkownika o podanym ID.");
            }
        }
    private void handleShowAllRentedVehicles() {
        System.out.println("\n--- LISTA WSZYSTKICH AKTYWNYCH WYPOŻYCZEŃ ---");

        java.util.List<carrent.models.Rental> activeRentals = rentalService.getAllActiveRentals();

        if (activeRentals.isEmpty()) {
            System.out.println("Obecnie żaden pojazd nie jest wypożyczony.");
            return;
        }

        System.out.printf("%-15s | %-15s | %-15s | %-20s%n", "UŻYTKOWNIK", "POJAZD", "NR REJ.", "DATA WYPOŻ.");
        System.out.println("---------------------------------------------------------------------------");

        for (carrent.models.Rental rental : activeRentals) {
            String userLogin = authService.getUserById(rental.getUserId())
                    .map(carrent.models.User::getLogin)
                    .orElse("Nieznany");

            String vehicleInfo = vehicleService.getVehicleById(rental.getVehicleId())
                    .map(v -> v.getBrand() + " " + v.getModel())
                    .orElse("Nieznany pojazd");

            String plate = vehicleService.getVehicleById(rental.getVehicleId())
                    .map(carrent.models.Vehicle::getPlate)
                    .orElse("???");

            System.out.printf("%-15s | %-15s | %-15s | %-20s%n",
                    userLogin, vehicleInfo, plate, rental.getRentDateTime());
        }

        System.out.println("\nNaciśnij Enter, aby wrócić...");
        scanner.nextLine();
    }

    private void handleAdminAdd() {
        try {
            System.out.println("--- Dodawanie pojazdu ---");
            System.out.print("Kategoria (np. Car, Motorcycle, Bus): ");
            String category = scanner.nextLine().trim();

            java.util.Map<String, Object> requiredAttributes = vehicleService.getCategoryAttributes(category);

            System.out.print("Marka: ");
            String brand = scanner.nextLine();
            System.out.print("Model: ");
            String model = scanner.nextLine();
            System.out.print("Rok produkcji: ");
            int year = Integer.parseInt(scanner.nextLine());
            System.out.print("Rejestracja: ");
            String plate = scanner.nextLine();
            System.out.print("Cena: ");
            double price = Double.parseDouble(scanner.nextLine());

            carrent.models.Vehicle vehicle = carrent.models.Vehicle.builder()
                    .category(category)
                    .brand(brand)
                    .model(model)
                    .year(year)
                    .plate(plate)
                    .price(price)
                    .attributes(new java.util.HashMap<>())
                    .build();

            if (requiredAttributes != null && !requiredAttributes.isEmpty()) {
                System.out.println("\n--- Wymagane atrybuty dla kategorii " + category + " ---");
                for (java.util.Map.Entry<String, Object> entry : requiredAttributes.entrySet()) {
                    String attrName = entry.getKey();
                    Object attrConfig = entry.getValue();
                    String expectedType;
                    java.util.List<String> allowed = null;

                    if (attrConfig instanceof java.util.Map) {
                        java.util.Map<String, Object> configMap = (java.util.Map<String, Object>) attrConfig;
                        expectedType = (String) configMap.get("type");
                        if (configMap.containsKey("allowed")) {
                            allowed = (java.util.List<String>) configMap.get("allowed");
                        }
                    } else {
                        expectedType = (String) attrConfig;
                    }

                    if (allowed != null && !allowed.isEmpty()) {
                        System.out.print("Podaj wartość dla '" + attrName + "' (dostępne opcje: " + String.join(", ", allowed) + "): ");
                    } else {
                        System.out.print("Podaj wartość dla '" + attrName + "' (wymagany typ danych: " + expectedType.toUpperCase() + "): ");
                    }
                    String value = scanner.nextLine();
                    vehicle.getAttributes().put(attrName, value);
                }
            } else {
                System.out.println("\nZaznaczona kategoria nie wymaga atrybutów dodatkowych.");
            }

            vehicleService.addVehicle(vehicle);
            System.out.println("Pojazd został dodany pomyślnie.");
        } catch (Exception e) {
            System.out.println("Błąd podczas dodawania pojazdu: " + e.getMessage());
        }
    }
}

