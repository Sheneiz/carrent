package carrent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserRepository implements IUserRepository {
    private final List<User> users = new ArrayList<>();
    private final String fileName = "users.csv";

    public UserRepository() {
        load();
    }

    @Override
    public User getUser(String login) {
        return users.stream()
                .filter(u -> u.getLogin().equals(login))
                .map(User::new)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> getUsers() {
        return users.stream().map(User::new).toList();
    }

    @Override
    public boolean addUser(User user) {
        if (getUser(user.getLogin()) != null) return false;
        users.add(new User(user));
        save();
        return true;
    }

    @Override
    public boolean removeUser(String login) {
        User user = getUser(login);
        if (user != null && (user.getRentedVehicleId() == null || user.getRentedVehicleId().isEmpty())) {
            users.removeIf(u -> u.getLogin().equals(login));
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getLogin().equals(user.getLogin())) {
                users.set(i, new User(user));
                save();
                return true;
            }
        }
        return false;
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (User u : users) {
                pw.println(u.getLogin() + ";" + u.getPassword() + ";" + u.getRole() + ";" +
                        (u.getRentedVehicleId() == null ? "" : u.getRentedVehicleId()));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        users.clear();
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isEmpty()) continue;
                String[] data = line.split(";", -1);
                users.add(new User(data[0], data[1], Role.valueOf(data[2]), data[3]));
            }
        } catch (FileNotFoundException ignored) {}
    }
}