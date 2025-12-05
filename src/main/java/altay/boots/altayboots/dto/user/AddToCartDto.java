package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Добавление товара в корзину")
public record AddToCartDto(

        @Schema(description = "ID товара", example = "10")
        int productId,

        @Schema(description = "Количество", example = "1")
        int quantity
) {}

