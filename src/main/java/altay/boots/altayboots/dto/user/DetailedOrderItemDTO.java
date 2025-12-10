package altay.boots.altayboots.dto.user;

public record DetailedOrderItemDTO(
        Integer order_item_id, // ID позиции заказа (OrderItem)
        Integer quantity,
        DetailedOrderProductDTO product
) {
}
