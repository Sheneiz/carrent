package carrent.repositories.impl.jdbc;

import carrent.db.DatabaseConnection;
import carrent.models.Rental;
import carrent.repositories.RentalRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RentalJdbcRepository implements RentalRepository {

    @Override
    public List<Rental> findAll() {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT id, vehicle_id, user_id, rent_date, return_date FROM rental";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapToRental(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Rental> findById(String id) {
        String sql = "SELECT id, vehicle_id, user_id, rent_date, return_date FROM rental WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToRental(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Rental> findByVehicleIdAndReturnDateIsNull(String vehicleId) {
        String sql = "SELECT id, vehicle_id, user_id, rent_date, return_date FROM rental WHERE vehicle_id = ? AND return_date IS NULL LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToRental(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Rental save(Rental rental) {
        if (rental.getId() == null || rental.getId().isBlank()) {
            rental.setId(UUID.randomUUID().toString());
        }

        String sql = """
            INSERT INTO rental (id, vehicle_id, user_id, rent_date, return_date) 
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE 
            SET vehicle_id = EXCLUDED.vehicle_id, 
                user_id = EXCLUDED.user_id, 
                rent_date = EXCLUDED.rent_date, 
                return_date = EXCLUDED.return_date;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rental.getId());
            pstmt.setString(2, rental.getVehicleId());
            pstmt.setString(3, rental.getUserId());
            pstmt.setString(4, rental.getRentDateTime());
            pstmt.setString(5, rental.getReturnDateTime());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rental;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM rental WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Rental mapToRental(ResultSet rs) throws SQLException {
        Rental r = new Rental();
        r.setId(rs.getString("id"));
        r.setVehicle(carrent.models.Vehicle.builder().id(rs.getString("vehicle_id")).build());
        r.setUser(carrent.models.User.builder().id(rs.getString("user_id")).build());
        r.setRentDateTime(rs.getString("rent_date"));
        r.setReturnDateTime(rs.getString("return_date"));
        return r;
    }
}
