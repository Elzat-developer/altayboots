package altay.boots.altayboots.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о пользователе")
public record UserDto(

        @Schema(description = "Имя пользователя", example = "Азамат")
        String name,

        @Schema(description = "Фамилия пользователя", example = "Жумабаев")
        String surName,

        @Schema(description = "Отчество пользователя", example = "Муратович")
        String lastName,

        @Schema(description = "Номер телефона", example = "+77071112233")
        String phone,

        @Schema(description = "Регион", example = "Костанайская область")
        String region,

        @Schema(description = "Город или район", example = "Костанай")
        String cityOrDistrict,

        @Schema(description = "Улица", example = "Абая")
        String street,

        @Schema(description = "Дом или квартира", example = "12/45")
        String houseOrApartment,

        @Schema(description = "Почтовый индекс", example = "110000")
        String indexPost
) {}

