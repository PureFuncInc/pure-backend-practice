package net.purefunc.practice.wallet.data.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.purefunc.practice.wallet.data.enu.WalletOperationType;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletVO {

    Long id;

    String username;

    String operationUuid;

    WalletOperationType operationType;

    BigDecimal beforeBalance;

    BigDecimal amount;

    BigDecimal afterBalance;

    String createBy;

    Long createDate;

    String lastModifiedBy;

    Long lastModifiedDate;
}
