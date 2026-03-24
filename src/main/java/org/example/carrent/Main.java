package carrent;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        IVehicleRepository vehicleRepo = new VehicleRepositoryImpl();
        IUserRepository userRepo = new UserRepository();
        Authentication auth = new Authentication(userRepo);
        Scanner scanner = new Scanner(System.in);
        User loggedUser = null;

        // - EKRAN STARTOWY: LOGOWANIE LUB REJESTRACJA -
        while (loggedUser == null) {
            System.out.println("\n--- WITAJ W WYPOŻYCZALNI ---");
            System.out.println("1. Logowanie");
            System.out.println("2. Rejestracja nowego użytkownika");
            System.out.println("3. Wyjście");
            System.out.print("Wybierz: ");
            String startChoice = scanner.nextLine();

            if (startChoice.equals("1")) {
                System.out.print("Login: ");
                String login = scanner.nextLine();
                System.out.print("Hasło: ");
                String pass = scanner.nextLine();
                loggedUser = auth.authenticate(login, pass);
                if (loggedUser == null) System.out.println("Błędne dane logowania!");

            } else if (startChoice.equals("2")) {
                System.out.print("Podaj nowy login: ");
                String newLogin = scanner.nextLine();
                System.out.print("Podaj nowe hasło: ");
                String newPass = scanner.nextLine();

                String hashed = Authentication.hashPassword(newPass);
                User newUser = new User(newLogin, hashed, Role.USER, "");

                if (userRepo.addUser(newUser)) {
                    System.out.println("Zarejestrowano pomyślnie! Możesz się teraz zalogować.");
                } else {
                    System.out.println("Błąd: Użytkownik o takim loginie już istnieje.");
                }

            } else if (startChoice.equals("3")) {
                return;
            }
        }

        System.out.println("\nZalogowano pomyślnie jako: " + loggedUser.getLogin());

        // - GŁÓWNA PĘTLA PROGRAMU -
        boolean running = true;
        while (running) {
            if (loggedUser.getRole() == Role.ADMIN) {
                System.out.println("\n--- MENU ADMINA ---");
                System.out.println("1. Lista pojazdów\n2. Dodaj pojazd\n3. Usuń pojazd\n4. Lista użytkowników\n5. Usuń użytkownika\n6. Wyjście");
                System.out.print("Wybierz: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> vehicleRepo.getVehicles().forEach(System.out::println);
                    case "2" -> {
                        System.out.print("ID: "); String id = scanner.nextLine();
                        vehicleRepo.add(new Car(id, "Marka", "Model", 2024, 100, false));
                        System.out.println("Dodano pojazd.");
                    }
                    case "3" -> {
                        System.out.print("Podaj ID do usunięcia: ");
                        if (vehicleRepo.remove(scanner.nextLine())) System.out.println("Usunięto pojazd.");
                        else System.out.println("Nie znaleziono pojazdu o tym ID.");
                    }
                    case "4" -> {
                        for (User u : userRepo.getUsers()) {
                            System.out.println(u);
                            if (u.getRentedVehicleId() != null && !u.getRentedVehicleId().isEmpty()) {
                                System.out.println(" -> " + vehicleRepo.getVehicle(u.getRentedVehicleId()));
                            }
                        }
                    }
                    case "5" -> {
                        System.out.print("Podaj login użytkownika do usunięcia: ");
                        String toDelete = scanner.nextLine();
                        if (userRepo.removeUser(toDelete)) {
                            System.out.println("Użytkownik usunięty.");
                        } else {
                            System.out.println("Błąd: Użytkownik nie istnieje lub posiada wypożyczony pojazd!");
                        }
                    }
                    case "6" -> running = false;
                }
            } else {
                System.out.println("\n--- MENU UŻYTKOWNIKA ---");
                System.out.println("1. Moje dane\n2. Wypożycz pojazd\n3. Zwróć pojazd\n4. Wyjście");
                System.out.print("Wybierz: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> {
                        System.out.println(loggedUser);
                        if (loggedUser.getRentedVehicleId() != null && !loggedUser.getRentedVehicleId().isEmpty()) {
                            System.out.println("Twój pojazd: " + vehicleRepo.getVehicle(loggedUser.getRentedVehicleId()));
                        }
                    }
                    case "2" -> {
                        if (loggedUser.getRentedVehicleId() != null && !loggedUser.getRentedVehicleId().isEmpty()) {
                            System.out.println("Już masz wypożyczony pojazd!");
                        } else {
                            System.out.print("Podaj ID pojazdu: ");
                            String id = scanner.nextLine();
                            if (vehicleRepo.rentVehicle(id)) {
                                loggedUser.setRentedVehicleId(id);
                                userRepo.update(loggedUser);
                                System.out.println("Wypożyczono!");
                            } else {
                                System.out.println("Pojazd niedostępny lub błędne ID.");
                            }
                        }
                    }
                    case "3" -> {
                        if (loggedUser.getRentedVehicleId() == null || loggedUser.getRentedVehicleId().isEmpty()) {
                            System.out.println("Nie masz nic do zwrócenia.");
                        } else {
                            if (vehicleRepo.returnVehicle(loggedUser.getRentedVehicleId())) {
                                loggedUser.setRentedVehicleId("");
                                userRepo.update(loggedUser);
                                System.out.println("Zwrócono pomyślnie!");
                            }
                        }
                    }
                    case "4" -> running = false;
                }
            }
        }
        System.out.println("Koniec programu.");
    }
}