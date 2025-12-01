package altay.boots.altayboots.dto.admin;

import altay.boots.altayboots.dto.status.PaidStatus;

import java.util.List;

public record GetProduct(
        String name,
        String description,
        String text,
        int price,
        int oldPrice,
        List<String> photos,
        int catalog_id,
        PaidStatus paidStatus
) {
}
