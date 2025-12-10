package altay.boots.altayboots.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record DetailedOrderDTO(
        Integer id,
        String name,
        LocalDateTime orderStartDate,
        String paidStatus, // Лучше String, чтобы избежать проблем с сериализацией Enum
        OrderUserDTO userDetails,
        List<DetailedOrderItemDTO> items
) {}
