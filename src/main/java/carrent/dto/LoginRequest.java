package carrent.dto;

public record LoginRequest(
        String login,
        String password
) {
}