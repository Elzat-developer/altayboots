package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Cart;
import altay.boots.altayboots.model.entity.CartItem;
import altay.boots.altayboots.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    List<CartItem> findByCartId(Integer cartId);
    CartItem findByCartAndProduct(Cart cart, Product product);
}
