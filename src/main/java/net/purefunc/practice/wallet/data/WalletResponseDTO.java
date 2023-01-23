package net.purefunc.practice.wallet.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponseDTO {

    Long fromId;

    BigDecimal fromBalance;

    Long toId;

    BigDecimal toBalance;
}
