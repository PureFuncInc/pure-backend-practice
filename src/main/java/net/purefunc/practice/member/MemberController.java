package net.purefunc.practice.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.purefunc.practice.config.security.LoginRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "👤 Member")
@RestController
@RequestMapping("/api/v1.0")
@SecurityRequirement(name = "Authentication")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "cc")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/members")
    ResponseEntity<MemberResponseDTO> getMembers(Principal principal) {
        return memberService
                .query(principal.getName())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("getMembers Err"));
    }

    @PostMapping("/members:login")
    ResponseEntity<String> postMembersLogin(@RequestBody LoginRequestDto loginRequestDto) {
        return memberService
                .login(loginRequestDto.getUsername(), loginRequestDto.getPassword())
                .map(v -> ResponseEntity
                        .status(HttpStatus.OK)
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", v))
                        .body(v)
                )
                .orElseThrow(() -> new RuntimeException("postMembersLogin Err"));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/members")
    ResponseEntity<Object> patchMembers(
            @RequestBody MemberPasswordRequestDTO memberPasswordRequestDTO,
            Principal principal) {
        return memberService
                .modifyPassword(principal.getName(), memberPasswordRequestDTO.oldPassword, memberPasswordRequestDTO.newPassword)
                .map(v -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new RuntimeException("patchMembers Err"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/members/{username}")
    ResponseEntity<Object> deleteMembers(@PathVariable String username) {
        return memberService
                .remove(username)
                .map(v -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new RuntimeException("deleteMembers Err"));
    }

    // 200, 201, http code
    // put, patch
    // id, uuid
}
