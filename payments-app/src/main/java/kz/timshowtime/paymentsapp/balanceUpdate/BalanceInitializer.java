package kz.timshowtime.paymentsapp.balanceUpdate;

import kz.timshowtime.paymentsapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceInitializer {
    private final WalletService walletService;

    @Value("${account.balance}")
    private double accountBalance;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        walletService.updateAllBalances(accountBalance)
                .doOnSuccess(res -> log.info("All balances initialized"))
                .block();
    }
}
