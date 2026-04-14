package carrent.repositories.impl;

import carrent.db.JsonFileStorage;
import carrent.models.Vehicle;
import carrent.repositories.VehicleRepository;
import com.google.gson.reflect.TypeToken;
import java.util.*;

public class VehicleJsonRepository implements VehicleRepository {
    private final JsonFileStorage<Vehicle> storage =
            new JsonFileStorage<>("vehicles.json", new TypeToken<List<Vehicle>>(){}.getType());
    private final List<Vehicle> vehicles;

    public VehicleJsonRepository() {
        List<Vehicle> loaded = storage.load();
        this.vehicles = (loaded != null) ? new ArrayList<>(loaded) : new ArrayList<>();
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles);
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return vehicles.stream().filter(v -> v.getId().equals(id)).findFirst();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        if(vehicle.getId() == null || vehicle.getId().isBlank()) {
            vehicle.setId(UUID.randomUUID().toString());
        } else {
            vehicles.removeIf(v -> v.getId().equals(vehicle.getId()));
        }
        vehicles.add(vehicle);
        storage.save(vehicles);
        return vehicle;
    }

    @Override
    public void deleteById(String id) {
        vehicles.removeIf(v -> v.getId().equals(id));
        storage.save(vehicles);
    }
}