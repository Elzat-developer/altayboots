package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Информация об акции")
public record GetPromotion(
        @Schema(description = "Название акции", example = "Черная пятница")
        String name,

        @Schema(description = "Описание акции", example = "Скидки до 70% на всё")
        String description,

        @Schema(description = "Список фото акции")
        List<String> photos,

        @Schema(description = "Размер скидки в процентах", example = "30")
        int percentageDiscounted,

        @Schema(description = "Глобальная акция?", example = "true")
        boolean global,

        @Schema(description = "ID каталога", example = "1")
        int catalogId,

        @Schema(description = "ID продукта", example = "10")
        int productId,

        @Schema(description = "Дата начала", example = "2025-01-15")
        String startDate,

        @Schema(description = "Дата окончания", example = "2025-01-20")
        String endDate
) {}

