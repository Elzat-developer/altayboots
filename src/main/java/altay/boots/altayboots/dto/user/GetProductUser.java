package altay.boots.altayboots.dto.user;

import java.util.List;

public record GetProductUser(
        String name,
        String description,
        String text,
        int price,
        int oldPrice,
        List<String> photos,
        int catalog_id
) {
}
