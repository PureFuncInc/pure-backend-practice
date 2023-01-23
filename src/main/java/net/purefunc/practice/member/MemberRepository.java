package net.purefunc.practice.member;

import net.purefunc.practice.member.data.MemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberPO, Long>, JpaSpecificationExecutor<MemberPO> {

    Optional<MemberPO> findByUsername(String username);
}
