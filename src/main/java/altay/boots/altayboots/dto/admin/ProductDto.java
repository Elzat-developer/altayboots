package altay.boots.altayboots.dto.admin;

import java.util.List;

public record ProductDto(
        Integer id,
        String name,
        String description,
        String text,
        Integer price,
        Integer oldPrice,
        Integer catalogId,
        List<ProductPhotoDto> photos
) {
}
