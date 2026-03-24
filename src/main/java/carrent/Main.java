package carrent;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        IVehicleRepository vehicleRepo = new VehicleRepositoryImpl();
        IUserRepository userRepo = new UserRepository();
        Authentication auth = new Authentication(userRepo);
        Scanner scanner = new Scanner(System.in);

        System.out.println("LOGOWANIE");
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String pass = scanner.nextLine();

        User loggedUser = auth.authenticate(login, pass);

        if (loggedUser == null) {
            System.out.println("Błędne dane logowania!");
            return;
        }

        boolean running = true;
        while (running) {
            if (loggedUser.getRole() == Role.ADMIN) {
                System.out.println("\n--- MENU ADMINA ---\n1. Lista pojazdów\n2. Dodaj pojazd\n3. Usuń pojazd\n4. Lista użytkowników\n5. Wyjście");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> vehicleRepo.getVehicles().forEach(System.out::println);
                    case "2" -> {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        vehicleRepo.add(new Car(id, "Marka", "Model", 2024, 100, false));
                    }
                    case "3" -> {
                        System.out.print("Podaj ID do usunięcia: ");
                        vehicleRepo.remove(scanner.nextLine());
                    }
                    case "4" -> {
                        for(User u : userRepo.getUsers()) {
                            System.out.println(u);
                            if (u.getRentedVehicleId() != null && !u.getRentedVehicleId().isEmpty()) {
                                System.out.println("  -> " + vehicleRepo.getVehicle(u.getRentedVehicleId()));
                            }
                        }
                    }
                    case "5" -> running = false;
                }
            } else {
                System.out.println("\n--- MENU UŻYTKOWNIKA ---\n1. Moje dane\n2. Wypożycz\n3. Zwróć\n4. Wyjście");
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> {
                        System.out.println(loggedUser);
                        if (loggedUser.getRentedVehicleId() != null && !loggedUser.getRentedVehicleId().isEmpty()) {
                            System.out.println("Twój pojazd: " + vehicleRepo.getVehicle(loggedUser.getRentedVehicleId()));
                        }
                    }
                    case "2" -> {
                        System.out.print("Podaj ID pojazdu: ");
                        String id = scanner.nextLine();
                        if (vehicleRepo.rentVehicle(id)) {
                            loggedUser.setRentedVehicleId(id);
                            userRepo.update(loggedUser);
                            System.out.println("Wypożyczono!");
                        }
                    }
                    case "3" -> {
                        if (vehicleRepo.returnVehicle(loggedUser.getRentedVehicleId())) {
                            loggedUser.setRentedVehicleId("");
                            userRepo.update(loggedUser);
                            System.out.println("Zwrócono!");
                        }
                    }
                    case "4" -> running = false;
                }
            }
        }
    }
}