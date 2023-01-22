package net.purefunc.practice.member;

import net.purefunc.practice.common.Status;
import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.wallet.WalletDAO;
import net.purefunc.practice.wallet.WalletPO;
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
    private final MemberDAO memberDAO;
    private final WalletDAO walletDAO;

    public MemberService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, MemberDAO memberDAO, WalletDAO walletDAO) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.memberDAO = memberDAO;
        this.walletDAO = walletDAO;
    }

    Optional<MemberResponseDTO> query(String username) {
        return memberDAO
                .findByUsername(username)
                .flatMap(v -> walletDAO
                        .findByMemberId(v.id)
                        .map(w -> MemberResponseDTO.builder()
                                .username(v.getUsername())
                                .balance(w.getBalance())
                                .build()
                        )
                );
    }

    Optional<String> login(String username, String password) {
        return memberDAO
                .findByUsername(username)
                .map(v -> {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                    return v;
                })
                .or(() -> {
                            final MemberPO memberPO = memberDAO.save(
                                    MemberPO.builder()
                                            .id(null)
                                            .username(username)
                                            .password(passwordEncoder.encode(password))
                                            .role(Role.ROLE_USER)
                                            .status(Status.ACTIVE)
                                            .build()
                            );
                            walletDAO.save(
                                    WalletPO.builder()
                                            .memberId(memberPO.id)
                                            .balance(BigDecimal.ZERO)
                                            .status(Status.ACTIVE)
                                            .build()
                            );

                            return Optional.of(memberPO);
                        }
                )
                .map(v -> jwtTokenService.generate(v.getUsername(), 60L * 60L * 1000L, UUID.randomUUID().toString()));
    }

    Optional<String> modifyPassword(String username, String oldPassword, String newPassword) {
        return memberDAO
                .findByUsername(username)
                .filter(v -> passwordEncoder.matches(oldPassword, newPassword))
                .map(v -> {
                    v.password = passwordEncoder.encode(newPassword);
                    final MemberPO memberPO = memberDAO.save(v);
                    return memberPO.username;
                });
    }

    Optional<String> remove(String username) {
        return memberDAO
                .findByUsername(username)
                .flatMap(v -> {
                    v.status = Status.FREEZE;
                    final MemberPO memberPO = memberDAO.save(v);
                    return walletDAO
                            .findByMemberId(memberPO.id)
                            .map(w -> {
                                w.setStatus(Status.FREEZE);
                                walletDAO.save(w);
                                return v.username;
                            });
                });
    }
}
