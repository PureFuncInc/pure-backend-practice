package net.purefunc.practice.wallet;

import net.purefunc.practice.wallet.data.WalletPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletPO, Long>, JpaSpecificationExecutor<WalletPO> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    Optional<WalletPO> findByMemberId(Long memberId);
}
