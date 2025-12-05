package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Фото товара")
public record ProductPhotoDto(

        @Schema(description = "URL фотографии товара",
                example = "https://your-site.com/uploads/product123_photo1.jpg")
        String photoURL
) {}

