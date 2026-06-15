package carrent.controllers;

import carrent.models.VehicleCategoryConfig;
import carrent.services.inter.VehicleServiceInterface;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final VehicleServiceInterface vehicleService;
    private final ObjectMapper objectMapper;

    public CategoryController(VehicleServiceInterface vehicleService, ObjectMapper objectMapper) {
        this.vehicleService = vehicleService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{category}")
    public VehicleCategoryConfig get(@PathVariable String category) {
        var attributes = vehicleService.getCategoryAttributes(category);
        return new VehicleCategoryConfig(category, attributes);
    }

    @GetMapping
    public List<VehicleCategoryConfig> list() {
        try (InputStream inputStream = new ClassPathResource("categories.json").getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się załadować kategorii z pliku", e);
        }
    }
}