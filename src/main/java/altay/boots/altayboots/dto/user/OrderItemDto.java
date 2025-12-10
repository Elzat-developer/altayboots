package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Товар в заказе")
public record OrderItemDto(
//        @Schema(description = "Имя продукта", example = "Зимние сопоги")
//        String productName,

        @Schema(description = "Количество", example = "1")
        int quantity,
//        @Schema(description = "Цена", example = "15000")
//        int productPrice,
//        @Schema(description = "Имя каталога", example = "Зимние")
//        String catalogName
        OrderItemProductDTO productInfo
) {}

