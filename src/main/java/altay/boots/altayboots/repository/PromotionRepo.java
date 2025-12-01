package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion,Integer> {
    Promotion findById(int promotion_id);
}
