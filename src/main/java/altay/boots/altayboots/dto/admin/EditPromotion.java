package altay.boots.altayboots.dto.admin;

public record EditPromotion(
        String name,
        String description,
        Integer percentageDiscounted,
        Boolean global,
        String startDate,
        String endDate
) {
}
