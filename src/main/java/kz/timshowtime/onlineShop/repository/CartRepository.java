package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    void deleteAllById(Cart cart);
}
