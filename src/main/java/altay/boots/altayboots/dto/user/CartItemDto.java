package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Позиция корзины")
public record CartItemDto(

        @Schema(description = "ID позиции", example = "1")
        Integer id,

        @Schema(description = "ID товара", example = "22")
        Integer productId,

        @Schema(description = "Название товара", example = "Куртка кожаная")
        String productName,

        @Schema(description = "Количество", example = "2")
        Integer quantity,

        @Schema(description = "Цена", example = "7990")
        Integer price
) {}

