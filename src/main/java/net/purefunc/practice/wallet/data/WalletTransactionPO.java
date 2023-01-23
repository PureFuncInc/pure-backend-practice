package net.purefunc.practice.wallet.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_transaction")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class WalletTransactionPO {

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
    String createBy;

    @CreatedDate
    Long createDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Long lastModifiedDate;
}
