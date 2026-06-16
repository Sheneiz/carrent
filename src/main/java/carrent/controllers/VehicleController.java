package carrent.controllers;

import carrent.models.Vehicle;
import carrent.services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final carrent.services.inter.VehicleServiceInterface vehicleService;

    public VehicleController(carrent.services.inter.VehicleServiceInterface vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<Vehicle> list(@RequestParam(name = "available", required = false, defaultValue = "false") boolean available) {
        if (available) {
            return vehicleService.getAvailableVehicles();
        }
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<Vehicle> get(@PathVariable String id) {
        return vehicleService.getVehicleById(id)
                .map(org.springframework.http.ResponseEntity::ok)
                .orElse(org.springframework.http.ResponseEntity.notFound().build()); // Zwróci 404 zamiast 400
    }

    @PostMapping
    public Vehicle create(@RequestBody Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            vehicle.setId(java.util.UUID.randomUUID().toString());
        }
        vehicleService.addVehicle(vehicle);
        return vehicle;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}