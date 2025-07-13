package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.Item;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

}
