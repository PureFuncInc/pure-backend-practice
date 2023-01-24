package net.purefunc.practice.member;

import net.purefunc.practice.member.data.enu.MemberRole;
import net.purefunc.practice.member.data.enu.MemberStatus;
import net.purefunc.practice.member.data.po.MemberPO;
import net.purefunc.practice.wallet.WalletRepository;
import net.purefunc.practice.wallet.data.enu.WalletStatus;
import net.purefunc.practice.wallet.data.po.WalletPO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    public MemberService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, MemberRepository memberRepository, WalletRepository walletRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
    }

    @Cacheable(cacheNames = {"members"}, key = "#username")
    public Optional<MemberPO> query(String username) {
        return memberRepository.findByUsername(username);
    }

    @CachePut(cacheNames = {"members"}, key = "#result.username")
    public Optional<MemberPO> login(String username, String password) {
        return memberRepository
                .findByUsername(username)
                .map(v -> {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                    return v;
                })
                .or(() -> {
                            MemberRole role = MemberRole.ROLE_USER;
                            if (username.equals("admin")) {
                                role = MemberRole.ROLE_ADMIN;
                            }

                            final MemberPO memberPO = memberRepository.save(
                                    MemberPO.builder()
                                            .id(null)
                                            .username(username)
                                            .password(passwordEncoder.encode(password))
                                            .about("")
                                            .role(role)
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
                );
    }

    @CachePut(cacheNames = {"members"}, key = "#result.username")
    public Optional<MemberPO> modifyAbout(String username, String about) {
        return memberRepository
                .findByUsername(username)
                .map(v -> {
                    v.setAbout(about);
                    return memberRepository.save(v);
                });
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

    @CacheEvict(cacheNames = {"members"}, key = "#username")
    public Optional<MemberPO> remove(String username) {
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
                                return v;
                            });
                });
    }
}
