package altay.boots.altayboots.dto.user;

import altay.boots.altayboots.dto.status.PaidStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Информация о заказе")
public record GetOrder(
        Integer order_id,

        @Schema(description = "Дата создания заказа", example = "2025-10-02T15:45:00")
        LocalDateTime orderStartDate,

        @Schema(description = "Статус оплаты")
        PaidStatus paidStatus,

        @Schema(description = "Позиции в заказе")
        List<OrderItemDto> items
) {}

