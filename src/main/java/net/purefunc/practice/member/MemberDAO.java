package net.purefunc.practice.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MemberDAO extends JpaRepository<MemberPO, Long>, JpaSpecificationExecutor<MemberPO> {

    Optional<MemberPO> findByUsername(String username);

    Page<MemberPO> findAllByCreateBy(String username, Pageable pageable);
}
