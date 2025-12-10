package altay.boots.altayboots.dto.admin;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о товаре")
public record ProductDto(

        @Schema(description = "ID товара", example = "12")
        Integer product_id,

        @Schema(description = "Название товара", example = "Кроссовки Nike Air Max")
        String name,

        @Schema(description = "Краткое описание товара", example = "Удобные повседневные кроссовки")
        String description,

        @Schema(description = "Детальный текст товара", example = "Полное описание всех характеристик товара")
        String text,

        @Schema(description = "Цена товара", example = "39990")
        Integer price,

        @Schema(description = "Старая цена товара", example = "49990")
        Integer oldPrice,

        @Schema(description = "ID каталога, к которому относится товар", example = "3")
        Integer catalogId,

        @Schema(description = "Фотографии товара")
        List<ProductPhotoDto> photos
) {}

