package carrent.services;

import carrent.models.Vehicle;
import java.time.Year;
import java.util.List;
import java.util.Map;

public class VehicleValidator {

    public void validate(Vehicle vehicle, Map<String, Object> requiredAttributes) {
        if (vehicle.getBrand() == null || vehicle.getBrand().trim().length() < 2) {
            throw new IllegalArgumentException("Marka musi mieć co najmniej 2 znaki.");
        }

        int currentYear = Year.now().getValue();
        if (vehicle.getYear() < 1900 || vehicle.getYear() > currentYear + 1) {
            throw new IllegalArgumentException("Nieprawidłowy rok produkcji.");
        }

        if (vehicle.getPlate() == null || !vehicle.getPlate().matches("(?i)^[A-Z0-9 ]{3,10}$")) {
            throw new IllegalArgumentException("Numer rejestracyjny musi mieć 3-10 znaków.");
        }

        validateAttributes(requiredAttributes, vehicle.getAttributes());
    }

    private void validateAttributes(Map<String, Object> requiredAttributes, Map<String, Object> providedAttributes) {
        if (requiredAttributes == null) return;

        for (Map.Entry<String, Object> entry : requiredAttributes.entrySet()) {
            String key = entry.getKey();
            
            String expectedType;
            List<String> allowed = null;
            
            if (entry.getValue() instanceof Map) {
                Map<String, Object> attrConfig = (Map<String, Object>) entry.getValue();
                expectedType = ((String) attrConfig.get("type")).toUpperCase();
                if (attrConfig.containsKey("allowed")) {
                    allowed = (List<String>) attrConfig.get("allowed");
                }
            } else {
                expectedType = ((String) entry.getValue()).toUpperCase();
            }

            Object value = providedAttributes.get(key);

            if (value == null) throw new IllegalArgumentException("Brak atrybutu: " + key);
            String valStr = value.toString().trim();

            if (!isTypeValid(value, expectedType)) {
                throw new IllegalArgumentException("Błędny typ dla " + key + ". Oczekiwano: " + expectedType);
            }

            if (allowed != null) {
                if (allowed.stream().noneMatch(a -> a.equalsIgnoreCase(valStr))) {
                    throw new IllegalArgumentException("Niepoprawna wartość dla " + key + ". Dozwolone: " + allowed);
                }
            }
        }
    }

    private boolean isTypeValid(Object value, String expectedType) {
        if (value == null) return false;
        String valStr = value.toString();
        return switch (expectedType) {
            case "STRING" -> true;
            case "INT", "INTEGER" -> valStr.matches("-?\\d+");
            case "DOUBLE" -> valStr.matches("-?\\d+(\\.\\d+)?");
            case "BOOLEAN" -> valStr.equalsIgnoreCase("true") || valStr.equalsIgnoreCase("false");
            default -> false;
        };
    }
}