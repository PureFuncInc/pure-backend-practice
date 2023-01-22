package net.purefunc.practice.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequestDTO {

    Long fromId;

    Long toId;

    BigDecimal amount;
}
