package net.purefunc.practice.wallet.data.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.purefunc.practice.wallet.data.enu.WalletOperationType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_transaction")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class WalletTransactionPO implements Serializable {

    private static final long serialVersionUID = -5376084452489538615L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    Long walletId;

    String operationUuid;

    @Enumerated(value = EnumType.STRING)
    WalletOperationType operationType;

    BigDecimal beforeBalance;

    BigDecimal amount;

    BigDecimal afterBalance;

    String memo;

    @CreatedBy
    String createdBy;

    @CreatedDate
    Long createdDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Long lastModifiedDate;
}
