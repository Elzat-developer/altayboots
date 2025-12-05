package altay.boots.altayboots.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Запрос на создание заказа")
public record CreateOrder(

        @Schema(description = "Товары заказа")
        List<OrderItemDto> items,

        @Schema(description = "Фамилия", example = "Иманов")
        String surName,

        @Schema(description = "Имя", example = "Даурен")
        String lastName,

        @Schema(description = "Регион", example = "Костанайская область")
        String region,

        @Schema(description = "Город или район", example = "Костанай")
        String cityOrDistrict,

        @Schema(description = "Улица", example = "Абая")
        String street,

        @Schema(description = "Дом / квартира", example = "15/20")
        String houseOrApartment,

        @Schema(description = "Индекс", example = "110000")
        String index
) {}

