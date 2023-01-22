package net.purefunc.practice.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WalletTransactionDAO extends JpaRepository<WalletTransactionPO, Long>, JpaSpecificationExecutor<WalletTransactionPO> {
}
