package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Описание компании")
public record CompanyDescription(
        @Schema(description = "Id компании", example = "1")
        int company_id,
        @Schema(description = "Название компании", example = "Kargaly Store")
        String name,

        @Schema(description = "Описание компании", example = "Магазин брендовой одежды и обуви")
        String text,

        @Schema(description = "URL изображения компании", example = "https://site.kz/company/photo.png")
        String photoURL,

        @Schema(description = "Месторасположение базы", example = "Рынок Кок-Базар")
        String base,

        @Schema(description = "Город", example = "Алматы")
        String city
) {}

