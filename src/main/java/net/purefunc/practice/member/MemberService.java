package net.purefunc.practice.member;

import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.member.data.MemberPO;
import net.purefunc.practice.member.data.MemberResponseDTO;
import net.purefunc.practice.member.data.MemberRole;
import net.purefunc.practice.member.data.MemberStatus;
import net.purefunc.practice.wallet.WalletRepository;
import net.purefunc.practice.wallet.data.WalletPO;
import net.purefunc.practice.wallet.data.WalletStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    public MemberService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, MemberRepository memberRepository, WalletRepository walletRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
    }

    Optional<MemberResponseDTO> query(String username) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v -> walletRepository
                        .findByMemberId(v.getId())
                        .map(w -> MemberResponseDTO.builder()
                                .username(v.getUsername())
                                .balance(w.getBalance())
                                .build()
                        )
                );
    }

    Optional<String> login(String username, String password) {
        return memberRepository
                .findByUsername(username)
                .map(v -> {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                    return v;
                })
                .or(() -> {
                            final MemberPO memberPO = memberRepository.save(
                                    MemberPO.builder()
                                            .id(null)
                                            .username(username)
                                            .password(passwordEncoder.encode(password))
                                            .role(MemberRole.ROLE_USER)
                                            .status(MemberStatus.ACTIVE)
                                            .build()
                            );
                            walletRepository.save(
                                    WalletPO.builder()
                                            .id(null)
                                            .memberId(memberPO.getId())
                                            .balance(BigDecimal.ZERO)
                                            .status(WalletStatus.ACTIVE)
                                            .build()
                            );

                            return Optional.of(memberPO);
                        }
                )
                .map(v -> jwtTokenService.generate(v.getUsername(), 60L * 60L * 1000L, UUID.randomUUID().toString()));
    }

    Optional<String> modifyPassword(String username, String oldPassword, String newPassword) {
        return memberRepository
                .findByUsername(username)
                .filter(v -> passwordEncoder.matches(oldPassword, v.getPassword()))
                .map(v -> {
                    v.setPassword(passwordEncoder.encode(newPassword));
                    final MemberPO memberPO = memberRepository.save(v);
                    return memberPO.getUsername();
                });
    }

    Optional<String> remove(String username) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v -> {
                    v.setStatus(MemberStatus.FREEZE);
                    final MemberPO memberPO = memberRepository.save(v);
                    return walletRepository
                            .findByMemberId(memberPO.getId())
                            .map(w -> {
                                w.setStatus(WalletStatus.FREEZE);
                                walletRepository.save(w);
                                return v.getUsername();
                            });
                });
    }
}
