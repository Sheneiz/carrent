package carrent.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategoryConfig {
    private String category;
    private Map<String, Object> attributes;
}