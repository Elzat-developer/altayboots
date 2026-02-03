// Новый DTO для EditProduct
package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

@Schema(description = "Данные для редактирования продукта")
public record EditProduct(
        @Schema(description = "Название продукта", example = "Nike Air Max 90")
        String name,
        @Schema(description = "Описание", example = "Обновленная версия модели")
        String description,
        @Schema(description = "Полное описание", example = "Лучшие кроссовки для повседневной носки")
        String text,
        @Schema(description = "Цена", example = "32990")
        Integer price,
        @Schema(description = "Старая цена", example = "37990")
        Integer oldPrice,
        @Schema(description = "Размеры", example = "[\"38\", \"39\", \"40\"]...")
        Set<String> sizes,
        @Schema(description = "Список ID фотографий", example = "[12, 13, 15]")
        List<Integer> photoIds,
        @Schema(description = "Ссылка от Ютуба", example = "https://www.youtube.com/watch?v=GeulXZP_kZ8")
        String youtubeUrl
) {}