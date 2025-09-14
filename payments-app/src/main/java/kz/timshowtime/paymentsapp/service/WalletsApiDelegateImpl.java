package kz.timshowtime.paymentsapp.service;


import kz.timshowtime.paymentsapp.gen.api.WalletsApiDelegate;
import kz.timshowtime.paymentsapp.gen.model.BalanceResponse;
import kz.timshowtime.paymentsapp.gen.model.ChargeRequest;
import kz.timshowtime.paymentsapp.gen.model.ChargeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class WalletsApiDelegateImpl implements WalletsApiDelegate {

    @Value("${account.balance:2000000.0}")
    private Double accountBalance;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        BalanceResponse balance = new BalanceResponse()
                .balance(accountBalance)
                .currency("KZT");

        return Mono.just(ResponseEntity.ok(balance));
    }

    @Override
    public Mono<ResponseEntity<ChargeResult>> charge(Mono<ChargeRequest> chargeRequest,
                                                     ServerWebExchange exchange) {
        return chargeRequest.flatMap(req -> {
            double amount = req.getAmount();

            if (amount <= 0) {
                ChargeResult negativeAmountErr = new ChargeResult()
                        .status(ChargeResult.StatusEnum.DECLINED)
                        .message("Amount must be greater than zero");
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(negativeAmountErr));
            }

            if (accountBalance < amount) {
                ChargeResult notEnoughMoneyErr = new ChargeResult()
                        .status(ChargeResult.StatusEnum.DECLINED)
                        .message("Not enough money, current balance: " + accountBalance);
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(notEnoughMoneyErr));
            }

            accountBalance -= amount;

            ChargeResult chargeResult = new ChargeResult()
                    .paymentId(UUID.randomUUID())
                    .debited(amount)
                    .balanceAfter(accountBalance)
                    .status(ChargeResult.StatusEnum.APPROVED)
                    .message("The payment was successful");

            return Mono.just(ResponseEntity.status(HttpStatus.OK).body(chargeResult));
        });
    }
}
