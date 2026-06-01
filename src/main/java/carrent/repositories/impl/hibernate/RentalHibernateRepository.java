package carrent.repositories.impl.hibernate;

import carrent.models.Rental;
import carrent.repositories.RentalRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
@org.springframework.context.annotation.Profile("hibernate")
public class RentalHibernateRepository implements RentalRepository {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Override
    public List<Rental> findAll() {
        return entityManager.createQuery("FROM Rental", Rental.class).getResultList();
    }

    @Override
    public Optional<Rental> findById(String id) {
        return Optional.ofNullable(entityManager.find(Rental.class, id));
    }

    @Override
    public Rental save(Rental rental) {
        return entityManager.merge(rental);
    }

    @Override
    public void deleteById(String id) {
        Rental rental = entityManager.find(Rental.class, id);
        if (rental != null) {
            entityManager.remove(rental);
        }
    }

    @Override
    public Optional<Rental> findByVehicleIdAndReturnDateIsNull(String vehicleId) {
        jakarta.persistence.TypedQuery<Rental> query = entityManager.createQuery(
                "FROM Rental r WHERE r.vehicle.id = :vehicleId AND r.returnDateTime IS NULL", Rental.class);
        query.setParameter("vehicleId", vehicleId);
        return query.getResultStream().findFirst();
    }
}
