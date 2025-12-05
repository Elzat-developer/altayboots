package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для редактирования акции")
public record EditPromotion(
        @Schema(description = "Название акции", example = "Летняя скидка")
        String name,

        @Schema(description = "Описание", example = "Скидка на летнюю коллекцию")
        String description,

        @Schema(description = "Скидка в процентах", example = "15")
        Integer percentageDiscounted,

        @Schema(description = "Глобальная акция?", example = "true")
        Boolean global,

        @Schema(description = "Дата начала", example = "2025-05-01")
        String startDate,

        @Schema(description = "Дата окончания", example = "2025-05-31")
        String endDate
) {}

