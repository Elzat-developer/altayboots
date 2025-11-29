package altay.boots.altayboots.dto.admin;

public record CreateProduct(
         String name,
         String description,
         String text,
         int price,
         int oldPrice,
         String photoURL,
         int catalog_id
) {
}
