package altay.boots.altayboots.dto.user;

import java.util.List;

public record CreateOrder(
        List<OrderItemDto> items
) {
}
