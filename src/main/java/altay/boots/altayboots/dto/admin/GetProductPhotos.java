package altay.boots.altayboots.dto.admin;

import java.util.List;

public record GetProductPhotos(
        Integer productId,
        List<GetPhotoDto> photoDto
) {
}
