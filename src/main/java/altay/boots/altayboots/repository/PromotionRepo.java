package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Promotion;
import altay.boots.altayboots.query.PromotionFirstImageProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion,Integer> {
    Promotion findById(int promotion_id);
    // В PromotionRepo
    @Query(value = "SELECT " +
            "p.id AS promotionId, " + // Псевдоним 'promotionId'
            "(SELECT pp.photo_url " +
            " FROM product_photo pp " +
            " WHERE pp.promotion_id = p.id " +
            " ORDER BY pp.id ASC " +
            " LIMIT 1) AS promotionImages " + // Псевдоним 'promotionImages'
            "FROM promotions p",
            nativeQuery = true)
    List<PromotionFirstImageProjection> findPromotionsWithFirstPhotoNative(); // <- Измененный тип
}
