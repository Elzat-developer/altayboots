package altay.boots.altayboots.dto.admin;

import java.util.List;

public record GetPromotion(
        String name,
        String description,
        List<String> photos,
        int percentageDiscounted,
        boolean global,
        int catalogId,
        int productId,
        String startDate,
        String endDate
) {
}
