package net.purefunc.practice.member.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.purefunc.practice.member.data.enu.MemberLoginType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginResponseDTO {

    String username;

    MemberLoginType type;

    String createdBy;

    String createdDateStr;
}
