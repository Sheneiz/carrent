package carrent.controllers;

import carrent.models.VehicleCategoryConfig;
import carrent.services.inter.VehicleServiceInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final VehicleServiceInterface vehicleService;

    public CategoryController(VehicleServiceInterface vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{category}")
    public VehicleCategoryConfig get(@PathVariable String category) {
        var attributes = vehicleService.getCategoryAttributes(category);
        return new VehicleCategoryConfig(category, attributes);
    }

    @GetMapping
    public List<VehicleCategoryConfig> list() {
        try (FileReader reader = new FileReader("categories.json")) {
            return new Gson().fromJson(reader, new TypeToken<List<VehicleCategoryConfig>>() {}.getType());
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się odczytać pliku konfiguracyjnego categories.json", e);
        }
    }
}