package altay.boots.altayboots.dto.admin;

public record CreatePromotion(
        String name,
        String description,
        int percentageDiscounted,
        boolean global,
        int catalogId,
        int productId,
        String startDate,
        String endDate
) {
}
