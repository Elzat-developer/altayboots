package altay.boots.altayboots.dto.admin;

public record UserDto(
        String name,
        String surName,
        String lastName,
        String phone,
        String region,
        String cityOrDistrict,
        String street,
        String houseOrApartment,
        String indexPost
) {
}
