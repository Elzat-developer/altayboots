package altay.boots.altayboots.dto.user;

import java.util.List;

public record CreateOrder(
        List<OrderItemDto> items,
        String surName,
        String lastName,
        String region,
        String cityOrDistrict,
        String street,
        String houseOrApartment,
        String index
) {
}
