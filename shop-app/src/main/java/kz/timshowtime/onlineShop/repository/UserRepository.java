package kz.timshowtime.onlineShop.repository;

import kz.timshowtime.onlineShop.model.ClientUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<ClientUser, Long> {
    Mono<ClientUser> findByUsername(String username);
}
