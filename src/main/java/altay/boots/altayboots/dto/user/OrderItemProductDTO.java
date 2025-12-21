package altay.boots.altayboots.dto.user;

import altay.boots.altayboots.dto.admin.GetPhotoDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OrderItemProductDTO(
        int productId,
        String productName,
        Integer productPrice, // Используйте тип, соответствующий полю 'price' в вашей сущности Product
        @Schema(description = "Список URL фотографий продукта")
        List<GetPhotoDto> photos,
        Boolean active,
        String catalogName
) {
}
