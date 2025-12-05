package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Корзина пользователя")
public record CartDto(

        @Schema(description = "ID корзины", example = "10")
        Integer cartId,

        @Schema(description = "Список товаров в корзине")
        List<CartItemDto> items,

        @Schema(description = "Общая стоимость", example = "15990")
        Integer totalPrice
) {}

