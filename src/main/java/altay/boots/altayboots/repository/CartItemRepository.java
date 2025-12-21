package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Cart;
import altay.boots.altayboots.model.entity.CartItem;
import altay.boots.altayboots.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    CartItem findByCartAndProduct(Cart cart, Product product);
    // ✅ Использование JOIN FETCH для одновременной загрузки CartItem и связанного Product
    // EAGER loading: Загружает CartItem, а также Product, связанный с каждым CartItem, в ОДНОМ запросе.
    // Используем INNER JOIN (по умолчанию для fetch), который гарантирует, что CartItem с NULL-Product
    // будут исключены или вызовут исключение на уровне базы, если product_id not null.
    // Если же product_id допускает NULL, то следует явно убедиться, что связь НЕ NULL (см. ниже)
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProducts(@Param("cartId") Integer cartId);
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
}
