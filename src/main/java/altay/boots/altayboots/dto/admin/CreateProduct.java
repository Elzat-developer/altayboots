package altay.boots.altayboots.dto.admin;

public record CreateProduct(
         String name,
         String description,
         String text,
         int price,
         int oldPrice,
         int catalog_id
) {
}
