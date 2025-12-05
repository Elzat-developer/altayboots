package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Товар в заказе")
public record OrderItemDto(

        @Schema(description = "ID продукта", example = "3")
        int productId,

        @Schema(description = "Количество", example = "1")
        int quantity
) {}

