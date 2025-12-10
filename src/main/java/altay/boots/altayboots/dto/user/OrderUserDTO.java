package altay.boots.altayboots.dto.user;

public record OrderUserDTO(
        Integer id,
        String name,
        String surName,
        String lastName,
        String region,
        String cityOrDistrict,
        String street,
        String houseOrApartment,
        String indexPost
) {}
