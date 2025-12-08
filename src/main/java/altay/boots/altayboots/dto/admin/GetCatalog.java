package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Получение каталога")
public record GetCatalog(
        @Schema(description = "Id каталога", example = "1")
        Integer catalog_id,
        @Schema(description = "Название каталога", example = "Одежда")
        String name
) {
}