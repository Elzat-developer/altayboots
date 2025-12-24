package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Product;
import altay.boots.altayboots.model.entity.ProductPhoto;
import altay.boots.altayboots.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPhotoRepo extends JpaRepository<ProductPhoto,Integer> {
    List<ProductPhoto> findAllByProduct(Product product);

    List<ProductPhoto> findAllByPromotion(Promotion promotion);
}
