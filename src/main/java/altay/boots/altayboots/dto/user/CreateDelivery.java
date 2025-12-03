package altay.boots.altayboots.dto.user;

public record CreateDelivery(
        String surName,
        String lastName,
        String region,
        String cityOrDistrict,
        String street,
        String houseOrApartment,
        String index
) {
}
