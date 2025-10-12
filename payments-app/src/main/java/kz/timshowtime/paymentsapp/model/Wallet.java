package kz.timshowtime.paymentsapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("wallets")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    @Id
    @Column("user_id")
    private Long userId;

    @Column("balance")
    private Double balance;

    @Column("currency")
    private String currency;
}