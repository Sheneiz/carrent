package carrent.dto;
import java.util.Map;

public record CategoryResponse(
        String category,
        Map<String, Object> attributes
) {}