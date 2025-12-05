package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Краткая информация о заказе для админа")
public record GetAdminOrderSimple(
        @Schema(description = "ID заказа", example = "1023")
        Integer orderId,

        @Schema(description = "Имя пользователя", example = "Иван Иванов")
        String userName,

        @Schema(description = "Дата создания заказа", example = "2025-12-04T12:33:41")
        LocalDateTime orderStartDate,

        @Schema(description = "Статус оплаты", example = "NOTPAY")
        PaidStatus paidStatus
) {}

