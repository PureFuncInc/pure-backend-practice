package net.purefunc.practice.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MemberDAO extends JpaRepository<MemberPO, Long>, JpaSpecificationExecutor<MemberPO> {

    Optional<MemberPO> findByUsername(String username);
}
