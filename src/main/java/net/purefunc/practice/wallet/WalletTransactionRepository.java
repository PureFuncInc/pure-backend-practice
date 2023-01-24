package net.purefunc.practice.wallet;

import net.purefunc.practice.wallet.data.po.WalletTransactionPO;
import net.purefunc.practice.wallet.data.vo.WalletVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionPO, Long>, JpaSpecificationExecutor<WalletTransactionPO> {

    @Query(value = "SELECT " +
            "new net.purefunc.practice.wallet.data.vo.WalletVO(wt.id, m.username, wt.operationUuid, wt.operationType, wt.beforeBalance, wt.amount, wt.afterBalance, wt.createdBy, wt.createdDate, wt.lastModifiedBy, wt.lastModifiedDate) " +
            "FROM WalletTransactionPO wt " +
            "INNER JOIN WalletPO w ON wt.walletId = w.id " +
            "INNER JOIN MemberPO m ON w.memberId = m.id " +
            "WHERE m.id = :memberId " +
            "ORDER BY wt.createdDate DESC")
    Page<WalletVO> findAllTxRecords(Long memberId, Pageable pageable);
}
