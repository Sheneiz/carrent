package carrent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VehicleRepositoryImpl implements IVehicleRepository {
    private List<Vehicle> vehicles = new ArrayList<>();
    private static final String File = "vehicles.csv";

    public VehicleRepositoryImpl() {
        load();
    }

    @Override
    public List<Vehicle> getVehicles() {
        List<Vehicle> copyList = new ArrayList<>();
        for (Vehicle v : vehicles) {
            copyList.add(v.copy());
        }
        return copyList;
    }

    @Override
    public boolean rentVehicle(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equals(id)) {
                if (!v.isRented()) {
                    v.setRented(true);
                    save();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean returnVehicle(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equals(id)) {
                if (v.isRented()) {
                    v.setRented(false);
                    save();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public void save() {
        try (PrintWriter out = new PrintWriter(new FileWriter(File))) {
            for (Vehicle v : vehicles) {
                out.println(v.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisywania danych do pliku: "+e.getMessage());
        }
    }

    @Override
    public void load() {
        vehicles.clear();
        File file = new File(File);
        if (!file.exists()) {
            return;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(";");
                String type = data[0];
                String id = data[1];
                String brand = data[2];
                String model = data[3];
                int year = Integer.parseInt(data[4]);
                double price = Double.parseDouble(data[5]);
                boolean rented = Boolean.parseBoolean(data[6]);

                if (type.equals("CAR")) {
                    vehicles.add(new Car(id, brand, model, year, price, rented));
                } else if (type.equals("MOTORCYCLE")) {
                    String category = data[7];
                    vehicles.add(new Motorcycle(id, brand, model, year, price, rented, category));
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas wczytywania danych: "+e.getMessage());
        }
    }
    @Override
    public Vehicle getVehicle(String id) {
        return vehicles.stream()
                .filter(v -> v.getId().equals(id))
                .map(Vehicle::copy)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean add(Vehicle vehicle) {
        if (getVehicle(vehicle.getId()) != null) return false;
        vehicles.add(vehicle.copy());
        save();
        return true;
    }

    @Override
    public boolean remove(String id) {
        boolean removed = vehicles.removeIf(v -> v.getId().equals(id));
        if (removed) save();
        return removed;
    }
}