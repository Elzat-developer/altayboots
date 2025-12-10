package altay.boots.altayboots.dto.user;

import java.util.List;

public record DetailedOrderProductDTO(
        Integer product_id,
        String name,
        String description,
        Integer price,
        Integer oldPrice,
        String catalogName, // Предполагаем, что вам нужно только имя каталога
        List<ProductPhotoDTO> photos
) {
}
