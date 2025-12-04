package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GetAdminOrder(
        Integer orderId,
        LocalDateTime orderStartDate,
        PaidStatus paidStatus,
        UserDto user,
        List<OrderItemFullDto> items
) {
}
