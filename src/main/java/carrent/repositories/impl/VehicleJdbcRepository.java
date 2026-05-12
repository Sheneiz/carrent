package carrent.repositories.impl;

import carrent.db.DatabaseConnection;
import carrent.models.Vehicle;
import carrent.repositories.VehicleRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VehicleJdbcRepository implements VehicleRepository {
    private final Gson gson = new Gson();
    private final Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT id, category, brand, model, year, plate, price, attributes FROM vehicle";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapToVehicle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        String sql = "SELECT id, category, brand, model, year, plate, price, attributes FROM vehicle WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
         if (vehicle.getId() == null || vehicle.getId().isBlank()) {
             vehicle.setId(UUID.randomUUID().toString());
         }

         String sql = """
            INSERT INTO vehicle (id, category, brand, model, year, plate, price, attributes) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
            ON CONFLICT (id) DO UPDATE 
            SET category = EXCLUDED.category, brand = EXCLUDED.brand, 
                model = EXCLUDED.model, year = EXCLUDED.year, 
                plate = EXCLUDED.plate, price = EXCLUDED.price, 
                attributes = EXCLUDED.attributes;
         """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vehicle.getId());
            pstmt.setString(2, vehicle.getCategory());
            pstmt.setString(3, vehicle.getBrand());
            pstmt.setString(4, vehicle.getModel());
            pstmt.setInt(5, vehicle.getYear());
            pstmt.setString(6, vehicle.getPlate());
            pstmt.setDouble(7, vehicle.getPrice());
            
            String attributesJson = vehicle.getAttributes() != null ? gson.toJson(vehicle.getAttributes()) : "{}";
            pstmt.setString(8, attributesJson);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicle;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM vehicle WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Vehicle mapToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getString("id"));
        v.setCategory(rs.getString("category"));
        v.setBrand(rs.getString("brand"));
        v.setModel(rs.getString("model"));
        v.setYear(rs.getInt("year"));
        v.setPlate(rs.getString("plate"));
        v.setPrice(rs.getDouble("price"));
        
        String attrJson = rs.getString("attributes");
        if (attrJson != null && !attrJson.isEmpty()) {
            Map<String, Object> attrs = gson.fromJson(attrJson, mapType);
            v.setAttributes(attrs != null ? attrs : new HashMap<>());
        }
        return v;
    }
}
