package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Изменение статуса заказа")
public record EditOrder(
        @Schema(description = "Статус оплаты", example = "PAID")
        PaidStatus paidStatus
) {}

