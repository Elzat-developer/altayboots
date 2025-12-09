package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Информация об акции")
public record GetPromotionFirstImage(
        @Schema(description = "Id акции", example = "1")
        Integer promotion_id,
        @Schema(description = "Первое фото акции")
        String photo
) {
}
