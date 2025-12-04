package altay.boots.altayboots.dto.admin;

public record EditProduct(
        String name,
        String description,
        String text,
        Integer price,
        Integer oldPrice
) {
}
