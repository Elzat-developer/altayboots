package altay.boots.altayboots.dto.user;

public record OrderItemProductDTO(
        int productId,
        String productName,
        Integer productPrice, // Используйте тип, соответствующий полю 'price' в вашей сущности Product
        String catalogName
) {
}
