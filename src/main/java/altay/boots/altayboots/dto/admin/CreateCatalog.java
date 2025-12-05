package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Создание нового каталога")
public record CreateCatalog(
        @Schema(description = "Название каталога", example = "Одежда")
        String name
) {}

