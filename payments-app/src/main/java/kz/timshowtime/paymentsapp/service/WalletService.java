package kz.timshowtime.paymentsapp.service;

import kz.timshowtime.paymentsapp.model.Wallet;
import kz.timshowtime.paymentsapp.repository.WalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepo walletRepo;

    public Mono<Wallet> getWalletByUserId(long userId) {
        return walletRepo.findByUserId(userId);
    }

    @Transactional
    public Mono<Void> updateAllBalances(double balance) {
        return walletRepo.updateAllBalance(balance);
    }

    @Transactional
    public Mono<Void> updateBalance(long userId, double amount) {
        return walletRepo.updateBalanceByUserId(userId, amount);
    }

    @Transactional
    public Mono<Wallet> save(Wallet wallet) {
        return walletRepo.insertWallet(wallet);
    }
}
