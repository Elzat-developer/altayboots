package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Редактирование товара в корзине")
public record EditCartItemDto(

        @Schema(description = "ID позиции корзины", example = "2")
        @NotNull(message = "cartItemId не может быть пустым")
        Integer cartItemId,

        @Schema(description = "Новое количество", example = "3")
        @NotNull(message = "quantity не может быть пустым")
        @Min(value = 1, message = "quantity должно быть > 0")
        Integer quantity
) {}


