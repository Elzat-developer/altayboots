package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Элемент заказа с полной информацией")
public record OrderItemFullDto(
        @Schema(description = "Продукт")
        ProductDto product,

        @Schema(description = "Количество товара в заказе", example = "3")
        Integer quantity
) {}

