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
        @Schema(description = "Область", example = "Область Абай")
        String base,
        @Schema(description = "Город", example = "Алматы")
        String city,
        @Schema(description = "Улица", example = "Абая 107")
        String street,
        @Schema(description = "Email", example = "erko008@gmail.com")
        String email,
        @Schema(description = "Номер компаний", example = "+77788136226")
        String phone,
        @Schema(description = "Начало работы", example = "09:00")
        String jobStart,
        @Schema(description = "Конец рабочего дня", example = "18:00")
        String jobEnd,
        @Schema(description = "Начало работы в выходные", example = "10:00")
        String freeStart,
        @Schema(description = "Конец работы в выходные", example = "14:00")
        String freeEnd,
        String userMainUrl
) {}

