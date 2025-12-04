package altay.boots.altayboots.repository;

import altay.boots.altayboots.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    User findByName(String username);
    User findByPhone(String phone);
}
