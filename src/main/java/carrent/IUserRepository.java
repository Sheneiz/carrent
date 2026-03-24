package carrent;

import java.util.List;

public interface IUserRepository {
    User getUser(String login);
    List<User> getUsers();
    boolean update(User user);
    boolean addUser(User user);
    boolean removeUser(String login);
}