package altay.boots.altayboots.query;

// Новый интерфейс, который будет выступать в качестве DTO/Projection
public interface PromotionFirstImageProjection {

    // ДОЛЖНО СОВПАДАТЬ с псевдонимом: p.id AS promotionId
    Integer getPromotionId();

    // ДОЛЖНО СОВПАДАТЬ с псевдонимом: ... AS promotionImages
    String getPromotionImages();
}
