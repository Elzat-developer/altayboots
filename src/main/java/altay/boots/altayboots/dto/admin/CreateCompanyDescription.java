package altay.boots.altayboots.dto.admin;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для создания описания компании")
public record CreateCompanyDescription(
        @Schema(description = "Название компании", example = "Kargaly Store")
        String name,

        @Schema(description = "Описание компании", example = "Лучший магазин одежды в Казахстане")
        String text,

        @Schema(description = "Локация базы", example = "Рынок Кок-Базар")
        String base,

        @Schema(description = "Город", example = "Алматы")
        String city
) {}

