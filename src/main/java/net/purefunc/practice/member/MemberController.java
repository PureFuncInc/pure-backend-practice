package net.purefunc.practice.member;

import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.config.security.LoginRequestDto;
import net.purefunc.practice.config.security.LoginResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1.0")
public class MemberController {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final MemberService memberService;
    private final MemberDAO memberDAO;

    public MemberController(
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            MemberService memberService,
            MemberDAO memberDAO
    ) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.memberService = memberService;
        this.memberDAO = memberDAO;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/members")
    ResponseEntity<MemberResponseDTO> getMembers(Principal principal) {
        return memberDAO
                .findByUsername(principal.getName())
                .map(v -> MemberResponseDTO.builder().username(v.getUsername()).build())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("getMembers Err"));
    }

    @PostMapping("/members:login")
    ResponseEntity<LoginResponseDto> postMembersLogin(@RequestBody LoginRequestDto loginRequestDto) {
        return memberDAO
                .findByUsername(loginRequestDto.getUsername())
                .map(v -> {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));
                    return v;
                })
                .or(() -> Optional
                        .of(memberDAO.save(
                                MemberPO.builder()
                                        .id(null)
                                        .username(loginRequestDto.getUsername())
                                        .password(passwordEncoder.encode(loginRequestDto.getPassword()))
                                        .role(Role.ROLE_USER)
                                        .status(Status.ACTIVE)
                                        .build()
                        ))
                )
                .map(v -> jwtTokenService.generate(v.getUsername(), 60L * 60L * 1000L, UUID.randomUUID().toString()))
                .map(v -> ResponseEntity
                        .status(HttpStatus.OK)
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", v))
                        .body(new LoginResponseDto(v))
                )
                .orElseThrow(() -> new RuntimeException("postMembersLogin Err"));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/members")
    ResponseEntity<Object> patchMembers(
            @RequestBody MemberPasswordRequestDTO memberPasswordRequestDTO,
            Principal principal
    ) {
        return memberDAO
                .findByUsername(principal.getName())
                .filter(v -> passwordEncoder.matches(memberPasswordRequestDTO.oldPassword, v.password))
                .map(v -> {
                    v.password = passwordEncoder.encode(memberPasswordRequestDTO.newPassword);
                    return memberDAO.save(v);
                })
                .map(v -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new RuntimeException("patchMembers Err"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/members/{username}")
    ResponseEntity<Object> deleteMembers(
            @PathVariable String username,
            Principal principal
    ) {
        return memberDAO
                .findByUsername(username)
                .map(v -> {
                    v.status = Status.FREEZE;
                    return memberDAO.save(v);
                })
                .map(v -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new RuntimeException("deleteMembers Err"));
    }

    // 200, 201
    // put
    // id, uuid
}
