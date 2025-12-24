package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для создания новой акции")
public record CreatePromotion(
        @Schema(description = "Название акции", example = "Новогодняя скидка")
        String name,

        @Schema(description = "Описание акции", example = "Скидка на зимнюю коллекцию")
        String description,

        @Schema(description = "Размер скидки", example = "20")
        Integer percentageDiscounted,

        @Schema(description = "Дата начала акции", example = "2025-01-01")
        String startDate,

        @Schema(description = "Дата окончания акции", example = "2025-01-10")
        String endDate
) {}

