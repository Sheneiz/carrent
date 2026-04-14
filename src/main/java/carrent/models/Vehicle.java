package carrent.models;

import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Vehicle {
    private String id;
    private String category;
    private String brand;
    private String model;
    private int year;
    private String plate;
    private double price;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public Vehicle copy() {
        return Vehicle.builder()
                .id(id)
                .category(category)
                .brand(brand)
                .model(model)
                .year(year)
                .plate(plate)
                .price(price)
                .attributes(new HashMap<>(attributes))
                .build();
    }
}