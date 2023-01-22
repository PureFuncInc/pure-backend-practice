package net.purefunc.practice.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WalletDAO extends JpaRepository<WalletPO, Long>, JpaSpecificationExecutor<WalletPO> {
}
