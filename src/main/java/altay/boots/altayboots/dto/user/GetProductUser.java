package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Товар для отображения пользователю")
public record GetProductUser(

        @Schema(description = "Название товара", example = "Пальто зимнее")
        String name,

        @Schema(description = "Описание товара", example = "Тёплое зимнее пальто")
        String description,

        @Schema(description = "Полное описание", example = "Материал: шерсть, подкладка...")
        String text,

        @Schema(description = "Цена", example = "29990")
        int price,

        @Schema(description = "Старая цена", example = "34990")
        int oldPrice,

        @Schema(description = "Список URL фотографий")
        List<String> photos,

        @Schema(description = "ID каталога", example = "5")
        int catalog_id
) {}

