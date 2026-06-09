package carrent.repositories.impl.hibernate;

import carrent.models.User;
import carrent.repositories.UserRepository;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
@org.springframework.context.annotation.Profile("hibernate")
public class UserHibernateRepository implements UserRepository {
    
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Override
    public List<User> findAll() {
        return entityManager.createQuery("FROM User", User.class).getResultList();
    }
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }
    @Override
    public Optional<User> findByLogin(String login) {
        jakarta.persistence.TypedQuery<User> query = entityManager.createQuery("FROM User WHERE login = :login", User.class);
        query.setParameter("login", login);
        return query.getResultStream().findFirst();
    }
    @Override
    public User save(User user) {
        return entityManager.merge(user);
    }
    @Override
    public void deleteById(String id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }
}
