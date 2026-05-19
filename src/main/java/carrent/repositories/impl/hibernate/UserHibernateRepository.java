package carrent.repositories.impl.hibernate;

import carrent.models.User;
import carrent.repositories.UserRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;
public class UserHibernateRepository implements UserRepository {
    private Session session;
    public void setSession(Session session) {
        this.session = session;
    }
    @Override
    public List<User> findAll() {
        return session.createQuery("FROM User", User.class).list();
    }
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(session.get(User.class, id));
    }
    @Override
    public Optional<User> findByLogin(String login) {
        Query<User> query = session.createQuery("FROM User WHERE login = :login", User.class);
        query.setParameter("login", login);
        return query.uniqueResultOptional();
    }
    @Override
    public User save(User user) {
        return session.merge(user);
    }
    @Override
    public void deleteById(String id) {
        User user = session.get(User.class, id);
        if (user != null) {
            session.remove(user);
        }
    }
}

