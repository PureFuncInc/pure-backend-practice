package net.purefunc.practice.wallet;

import net.purefunc.practice.wallet.data.WalletTransactionPO;
import net.purefunc.practice.wallet.data.WalletVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionPO, Long>, JpaSpecificationExecutor<WalletTransactionPO> {

    @Query(value = "SELECT " +
            "wt.id, m.username, wt.operationUuid, wt.operationType, wt.beforeBalance, wt.amount, wt.afterBalance, wt.createBy, wt.createDate, wt.lastModifiedBy, wt.lastModifiedDate " +
            "FROM WalletTransactionPO wt " +
            "INNER JOIN WalletPO w ON wt.walletId = w.id " +
            "INNER JOIN MemberPO m ON w.memberId = m.id " +
            "ORDER BY wt.createDate DESC")
    Page<WalletVO> findAllTxRecords(Long memberId, Pageable pageable);
}
