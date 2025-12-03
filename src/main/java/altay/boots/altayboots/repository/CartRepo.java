package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.Cart;
import altay.boots.altayboots.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends JpaRepository<Cart,Integer> {
    Cart findByUser(User user);
}
