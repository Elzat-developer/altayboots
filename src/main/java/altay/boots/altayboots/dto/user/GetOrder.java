package altay.boots.altayboots.dto.user;

import altay.boots.altayboots.dto.status.PaidStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GetOrder(
        String name,
        LocalDateTime orderStartDate,
        PaidStatus paidStatus,
        List<OrderItemDto> items
) {
}
