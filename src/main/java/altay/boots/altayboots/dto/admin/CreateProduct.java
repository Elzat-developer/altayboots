package altay.boots.altayboots.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record CreateProduct(
         String name,
         String description,
         String text,
         int price,
         int oldPrice,
         MultipartFile photoURL,
         int catalog_id
) {
}
