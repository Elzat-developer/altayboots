package altay.boots.altayboots.dto.user;

public record CartItemDto(
        Integer id,
        Integer productId,
        String productName,
        Integer quantity,
        Integer price
) {
}
