package br.com.devquote.utils;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import java.util.*;

@UtilityClass
public final class SortUtils {

    public static Sort buildAndSanitize(List<String> sortParams, Set<String> allowedFields, String defaultField) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(Sort.Order.asc(defaultField));
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String raw : sortParams) {
            if (raw == null || raw.isBlank()) continue;

            String[] parts = raw.split(",", 2);
            String field = parts[0].trim();
            if (!allowedFields.contains(field)) continue;

            Sort.Direction dir = Sort.Direction.ASC;
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                dir = Sort.Direction.DESC;
            }
            orders.add(new Sort.Order(dir, field));
        }

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.asc(defaultField));
        }

        boolean hasDefault = orders.stream().anyMatch(o -> o.getProperty().equals(defaultField));
        if (!hasDefault) orders.add(Sort.Order.asc(defaultField));
        return Sort.by(orders);
    }
}
