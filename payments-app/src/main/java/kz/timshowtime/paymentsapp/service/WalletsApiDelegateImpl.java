package kz.timshowtime.paymentsapp.service;


import kz.timshowtime.paymentsapp.gen.api.WalletsApiDelegate;
import kz.timshowtime.paymentsapp.gen.model.BalanceResponse;
import kz.timshowtime.paymentsapp.gen.model.ChargeRequest;
import kz.timshowtime.paymentsapp.gen.model.ChargeResult;
import kz.timshowtime.paymentsapp.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletsApiDelegateImpl implements WalletsApiDelegate {

    private final WalletService walletService;

    @Value("${account.balance:2000000.0}")
    private Double accountBalance;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(Long userId, ServerWebExchange exchange) {
        return walletService.getWalletByUserId(userId)
                .switchIfEmpty(createNewWallet(userId))
                .map(wallet -> new BalanceResponse()
                        .balance(wallet.getBalance())
                        .currency(wallet.getCurrency()))
                .map(ResponseEntity::ok);

    }

    @Override
    public Mono<ResponseEntity<ChargeResult>> charge(Long userId,
                                                     Mono<ChargeRequest> chargeRequest,
                                                     ServerWebExchange exchange) {
        return walletService.getWalletByUserId(userId)
                .flatMap(wallet -> chargeRequest.flatMap(req -> {
                            double balance = wallet.getBalance();
                            double amount = req.getAmount();

                            if (amount <= 0) {
                                ChargeResult negativeAmountErr = new ChargeResult()
                                        .status(ChargeResult.StatusEnum.DECLINED)
                                        .message("Amount must be greater than zero");
                                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(negativeAmountErr));
                            }

                            if (balance < amount) {
                                ChargeResult notEnoughMoneyErr = new ChargeResult()
                                        .status(ChargeResult.StatusEnum.DECLINED)
                                        .message("Not enough money, current balance: " + accountBalance);
                                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(notEnoughMoneyErr));
                            }

                            double newBalance = balance - amount;

                            return walletService.updateBalance(userId, newBalance)
                                    .thenReturn(ResponseEntity.ok(
                                            new ChargeResult()
                                                    .paymentId(UUID.randomUUID())
                                                    .debited(amount)
                                                    .balanceAfter(newBalance)
                                                    .status(ChargeResult.StatusEnum.APPROVED)
                                                    .message("The payment was successful")
                                    ));

                        })
                );
    }

    private Mono<Wallet> createNewWallet(Long userId) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(accountBalance)
                .currency("KZT")
                .build();
        return walletService.save(wallet);
    }
}
