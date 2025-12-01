package altay.boots.altayboots.dto.admin;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePromotion(
        String name,
        String description,
        List<MultipartFile> photos,
        int percentageDiscounted,
        boolean global,
        int catalogId,
        int productId,
        String startDate,
        String endDate
) {
}
