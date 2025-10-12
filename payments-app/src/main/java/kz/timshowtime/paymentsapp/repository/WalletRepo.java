package kz.timshowtime.paymentsapp.repository;

import kz.timshowtime.paymentsapp.model.Wallet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepo extends ReactiveCrudRepository<Wallet, Long> {
    Mono<Wallet> findByUserId(Long userId);

    @Query("UPDATE wallets set balance = :balance")
    Mono<Void> updateAllBalance(@Param("balance") double balance);

    @Query("UPDATE wallets SET balance = :balance WHERE user_id = :userId")
    Mono<Void> updateBalanceByUserId(@Param("userId") Long userId, @Param("balance") Double balance);

    @Query("""
                INSERT INTO wallets (user_id, balance, currency)
                VALUES (:#{#wallet.userId}, :#{#wallet.balance}, :#{#wallet.currency})
                RETURNING user_id, user_id, balance, currency
    """)
    Mono<Wallet> insertWallet(@Param("wallet") Wallet wallet);

}