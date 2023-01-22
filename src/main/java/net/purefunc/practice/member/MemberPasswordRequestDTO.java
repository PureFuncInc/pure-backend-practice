package net.purefunc.practice.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPasswordRequestDTO {

    String oldPassword;
    String newPassword;
}
