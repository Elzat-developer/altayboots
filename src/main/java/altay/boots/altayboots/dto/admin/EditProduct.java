// Новый DTO для EditProduct
package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List; // ⚠️ ДОБАВЛЯЕМ List

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

        // 🚨 НОВОЕ ПОЛЕ: Список ID существующих фото, которые нужно удалить
        @Schema(description = "Список ID старых фото (102 и 104 удалены) и заглушек для новых файлов.")
        List<String> finalPhotoOrder
) {}