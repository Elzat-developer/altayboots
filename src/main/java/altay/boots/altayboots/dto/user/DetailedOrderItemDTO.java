package altay.boots.altayboots.dto.user;

public record DetailedOrderItemDTO(
        Integer id, // ID позиции заказа (OrderItem)
        Integer quantity,
        DetailedOrderProductDTO product
) {
}
