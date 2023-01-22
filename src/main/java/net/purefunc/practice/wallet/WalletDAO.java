package net.purefunc.practice.wallet;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface WalletDAO extends JpaRepository<WalletPO, Long>, JpaSpecificationExecutor<WalletPO> {

    @Query(value = "")
    WalletPO findAllTxRecords(Long memberId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletPO> findByMemberId(Long memberId);
}
