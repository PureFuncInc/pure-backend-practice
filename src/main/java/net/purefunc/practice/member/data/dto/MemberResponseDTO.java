package net.purefunc.practice.member.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.purefunc.practice.member.data.enu.MemberRole;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDTO {

    Long id;

    String username;

    String about;

    String avatarLink;

    MemberRole role;

    String createdDateStr;
}
