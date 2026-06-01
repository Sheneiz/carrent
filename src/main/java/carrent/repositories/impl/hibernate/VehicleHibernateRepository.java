package carrent.repositories.impl.hibernate;

import carrent.models.Vehicle;
import carrent.repositories.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("hibernate")
public class VehicleHibernateRepository implements VehicleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Vehicle> findAll() {
        return entityManager.createQuery("FROM Vehicle", Vehicle.class).getResultList();
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(entityManager.find(Vehicle.class, id));
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return entityManager.merge(vehicle);
    }

    @Override
    public void deleteById(String id) {
        Vehicle vehicle = entityManager.find(Vehicle.class, id);
        if (vehicle != null) {
            entityManager.remove(vehicle);
        }
    }
}