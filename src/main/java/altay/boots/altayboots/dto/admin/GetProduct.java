package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Информация о продукте")
public record GetProduct(
        @Schema(description = "Id продукта", example = "1")
        Integer id,
        @Schema(description = "Название продукта", example = "Кроссовки Adidas UltraBoost")
        String name,

        @Schema(description = "Краткое описание продукта", example = "Удобные и легкие беговые кроссовки")
        String description,

        @Schema(description = "Полное описание", example = "Подходят для ежедневного использования и спорта")
        String text,

        @Schema(description = "Цена продукта", example = "39990")
        int price,

        @Schema(description = "Старая цена", example = "49990")
        int oldPrice,

        @Schema(description = "Список URL фотографий продукта")
        List<String> photos,

        @Schema(description = "ID каталога, к которому принадлежит продукт", example = "2")
        Integer catalog_id
) {}

