package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для создания продукта")
public record CreateProduct(
        @Schema(description = "Название продукта", example = "Nike Air Force 1")
        String name,

        @Schema(description = "Описание", example = "Стильные кожаные кроссовки")
        String description,

        @Schema(description = "Полное описание", example = "Популярная модель в классическом стиле")
        String text,

        @Schema(description = "Цена", example = "29990")
        Integer price,

        @Schema(description = "Старая цена", example = "34990")
        Integer oldPrice,

        @Schema(description = "ID каталога", example = "1")
        int catalog_id
) {}

