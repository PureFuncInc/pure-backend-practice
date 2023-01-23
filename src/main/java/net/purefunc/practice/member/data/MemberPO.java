package net.purefunc.practice.member.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    MemberRole role;

    @Enumerated(value = EnumType.STRING)
    MemberStatus status;

    @CreatedBy
    String createBy;

    @CreatedDate
    Long createDate;

    @LastModifiedBy
    String lastModifiedBy;

    @LastModifiedDate
    Long lastModifiedDate;
}
