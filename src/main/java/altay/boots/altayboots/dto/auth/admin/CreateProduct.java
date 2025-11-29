package altay.boots.altayboots.dto.auth.admin;

public record CreateProduct(
         String name,
         String description,
         String text,
         int price,
         int oldPrice,
         String photoURL
) {
}
