package altay.boots.altayboots.dto.user;

import java.util.List;

public record CartDto(
        Integer cartId,
        List<CartItemDto> items,
        Integer totalPrice
) {
}
