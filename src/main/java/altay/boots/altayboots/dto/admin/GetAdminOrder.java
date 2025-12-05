package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Полная информация о заказе для админа")
public record GetAdminOrder(
        @Schema(description = "ID заказа", example = "1023")
        Integer orderId,

        @Schema(description = "Дата создания заказа", example = "2025-12-04T12:33:41")
        LocalDateTime orderStartDate,

        @Schema(description = "Статус оплаты", example = "PAID")
        PaidStatus paidStatus,

        @Schema(description = "Информация о пользователе")
        UserDto user,

        @Schema(description = "Список товаров в заказе")
        List<OrderItemFullDto> items
) {}

