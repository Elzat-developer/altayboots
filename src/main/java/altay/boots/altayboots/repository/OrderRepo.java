package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order,Integer> {
    List<Order> findAllByUserId(int userId);
    Order findById(int orderId);
    // В OrderRepo
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH p.photos " + // Добавляем ProductPhoto
            "LEFT JOIN FETCH o.user " +   // Добавляем User
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Integer orderId);
}
