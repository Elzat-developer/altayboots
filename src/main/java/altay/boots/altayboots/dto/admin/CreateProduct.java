package altay.boots.altayboots.dto.admin;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateProduct(
         String name,
         String description,
         String text,
         int price,
         int oldPrice,
         List<MultipartFile> photos,
         int catalog_id
) {
}
