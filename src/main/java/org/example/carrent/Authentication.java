package carrent;

import org.apache.commons.codec.digest.DigestUtils;

public class Authentication {
    private final IUserRepository userRepository;

    public Authentication(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String login, String password) {
        User user = userRepository.getUser(login);
        if (user != null) {
            String hashedPassword = hashPassword(password);
            if (user.getPassword().equals(hashedPassword)) {
                return user;
            }
        }
        return null;
    }

    public static String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
}