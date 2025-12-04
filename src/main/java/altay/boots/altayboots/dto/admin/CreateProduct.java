package altay.boots.altayboots.dto.admin;

public record CreateProduct(
         String name,
         String description,
         String text,
         Integer price,
         Integer oldPrice,
         int catalog_id
) {
}
