package carrent.repositories.impl.hibernate;

import carrent.models.Vehicle;
import carrent.repositories.VehicleRepository;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;
public class VehicleHibernateRepository implements VehicleRepository {
    private Session session;
    public void setSession(Session session) {
        this.session = session;
    }
    @Override
    public List<Vehicle> findAll() {
        return session.createQuery("FROM Vehicle", Vehicle.class).list();
    }
    @Override
    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(session.get(Vehicle.class, id));
    }
    @Override
    public Vehicle save(Vehicle vehicle) {
        return session.merge(vehicle);
    }
    @Override
    public void deleteById(String id) {
        Vehicle vehicle = session.get(Vehicle.class, id);
        if (vehicle != null) {
            session.remove(vehicle);
        }
    }
}

