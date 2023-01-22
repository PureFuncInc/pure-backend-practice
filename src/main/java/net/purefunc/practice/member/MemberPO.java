package net.purefunc.practice.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.purefunc.practice.common.Status;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class MemberPO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(unique = true)
    String username;

    String password;

    @Enumerated(value = EnumType.STRING)
    Role role;

    @Enumerated(value = EnumType.STRING)
    Status status;

    @CreatedBy
    String createBy;

    @CreatedDate
    Long createDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Long lastModifiedDate;
}
