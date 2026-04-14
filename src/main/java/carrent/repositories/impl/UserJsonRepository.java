package carrent.repositories.impl;

import carrent.db.JsonFileStorage;
import carrent.models.User;
import carrent.repositories.UserRepository;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserJsonRepository implements UserRepository {
    private final JsonFileStorage<User> storage =
            new JsonFileStorage<>("users.json", new TypeToken<List<User>>(){}.getType());    private final List<User> users;

    public UserJsonRepository() {
        List<User> loaded = storage.load();
        this.users = (loaded != null) ? new ArrayList<>(loaded) : new ArrayList<>();
    }

    @Override
    public List<User> findAll() {
        return users.stream().map(User::copy).toList();
    }

    @Override
    public Optional<User> findById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).map(User::copy).findFirst();
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return users.stream().filter(u -> u.getLogin().equals(login)).map(User::copy).findFirst();
    }

    @Override
    public User save(User user) {
        User toSave = user.copy();
        if (toSave.getId() == null || toSave.getId().isBlank()) {
            toSave.setId(UUID.randomUUID().toString());
        } else {
            users.removeIf(u -> u.getId().equals(toSave.getId()));
        }
        users.add(toSave);
        storage.save(users);
        return toSave;
    }

    @Override
    public void deleteById(String id) {
        users.removeIf(u -> u.getId().equals(id));
        storage.save(users);
    }
}