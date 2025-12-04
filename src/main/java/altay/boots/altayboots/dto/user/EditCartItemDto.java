package altay.boots.altayboots.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EditCartItemDto(
        @NotNull(message = "cartItemId не может быть пустым") Integer cartItemId,
        @NotNull(message = "quantity не может быть пустым")
        @Min(value = 1, message = "quantity должно быть > 0") Integer quantity
) {}

