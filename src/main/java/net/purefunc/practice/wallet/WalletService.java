package net.purefunc.practice.wallet;

import net.purefunc.practice.member.MemberRepository;
import net.purefunc.practice.wallet.data.dto.WalletOpResponseDTO;
import net.purefunc.practice.wallet.data.dto.WalletResponseDTO;
import net.purefunc.practice.wallet.data.enu.WalletOperationType;
import net.purefunc.practice.wallet.data.po.WalletTransactionPO;
import net.purefunc.practice.wallet.data.vo.WalletVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(MemberRepository memberRepository, WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository) {
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public WalletResponseDTO query(String username) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v ->
                        walletRepository
                                .findByMemberId(v.getId())
                                .map(w -> WalletResponseDTO
                                        .builder()
                                        .memberId(w.getMemberId())
                                        .username(v.getUsername())
                                        .balance(w.getBalance())
                                        .build()
                                )
                )
                .orElseThrow(() -> new RuntimeException("WalletService query Err"));
    }

    public Page<WalletVO> queryTransactions(String username, Integer page, Integer size) {
        return memberRepository
                .findByUsername(username)
                .map(v -> walletTransactionRepository.findAllTxRecords(v.getId(), PageRequest.of(page, size)))
                .orElseThrow(() -> new RuntimeException("WalletService queryTransactions Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    public WalletOpResponseDTO deposit(
            String username,
            BigDecimal amount) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v -> walletRepository
                        .findByMemberId(v.getId())
                        .map(w -> {
                            final BigDecimal beforeBalance = w.getBalance();
                            final BigDecimal afterBalance = w.getBalance().add(amount);
                            w.setBalance(afterBalance);
                            walletRepository.save(w);

                            return walletTransactionRepository.save(
                                    WalletTransactionPO
                                            .builder()
                                            .id(null)
                                            .walletId(w.getId())
                                            .operationUuid(UUID.randomUUID().toString())
                                            .operationType(WalletOperationType.DEPOSIT)
                                            .beforeBalance(beforeBalance)
                                            .amount(amount)
                                            .afterBalance(afterBalance)
                                            .memo(WalletOperationType.DEPOSIT.name() + "to " + w.getMemberId())
                                            .build()
                            );
                        }))
                .map(v -> WalletOpResponseDTO
                        .builder()
                        .fromMemberId(v.getWalletId())
                        .fromMemberBalance(v.getBeforeBalance())
                        .toMemberId(v.getWalletId())
                        .toMemberBalance(v.getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService deposit Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    public WalletOpResponseDTO withdraw(
            String username,
            BigDecimal amount) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v -> walletRepository
                        .findByMemberId(v.getId())
                        .map(w -> {
                            final BigDecimal beforeBalance = w.getBalance();
                            final BigDecimal afterBalance = w.getBalance().subtract(amount);
                            w.setBalance(afterBalance);
                            walletRepository.save(w);

                            return walletTransactionRepository.save(
                                    WalletTransactionPO
                                            .builder()
                                            .id(null)
                                            .walletId(w.getId())
                                            .operationUuid(UUID.randomUUID().toString())
                                            .operationType(WalletOperationType.WITHDRAW)
                                            .beforeBalance(beforeBalance)
                                            .amount(amount)
                                            .afterBalance(afterBalance)
                                            .memo(WalletOperationType.WITHDRAW.name() + "from " + w.getMemberId())
                                            .build()
                            );
                        }))
                .map(v -> WalletOpResponseDTO
                        .builder()
                        .fromMemberId(v.getWalletId())
                        .fromMemberBalance(v.getBeforeBalance())
                        .toMemberId(v.getWalletId())
                        .toMemberBalance(v.getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService deposit Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    public WalletOpResponseDTO transfer(
            String username,
            Long toId,
            BigDecimal amount) {
        return memberRepository
                .findByUsername(username)
                .flatMap(v -> walletRepository
                        .findByMemberId(v.getId())
                        .flatMap(w1 -> walletRepository
                                .findByMemberId(toId)
                                .map(w2 -> {
                                    final BigDecimal beforeBalanceW1 = w1.getBalance();
                                    final BigDecimal afterBalanceW1 = w1.getBalance().subtract(amount);
                                    w1.setBalance(afterBalanceW1);
                                    walletRepository.save(w1);

                                    final BigDecimal beforeBalanceW2 = w2.getBalance();
                                    final BigDecimal afterBalanceW2 = w2.getBalance().add(amount);
                                    w2.setBalance(afterBalanceW2);
                                    walletRepository.save(w2);

                                    final String operationUuid = UUID.randomUUID().toString();
                                    final WalletTransactionPO walletTransactionPO1 = walletTransactionRepository.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w1.getId())
                                                    .operationUuid(operationUuid)
                                                    .operationType(WalletOperationType.TRANSFER_OUT)
                                                    .beforeBalance(beforeBalanceW1)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW1)
                                                    .memo(WalletOperationType.TRANSFER_OUT.name() + "to " + w2.getMemberId())
                                                    .build()
                                    );

                                    final WalletTransactionPO walletTransactionPO2 = walletTransactionRepository.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w2.getId())
                                                    .operationUuid(operationUuid)
                                                    .operationType(WalletOperationType.TRANSFER_IN)
                                                    .beforeBalance(beforeBalanceW2)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW2)
                                                    .memo(WalletOperationType.TRANSFER_IN.name() + "from " + w1.getMemberId())
                                                    .build()
                                    );

                                    return Pair.of(walletTransactionPO1, walletTransactionPO2);
                                }))
                )
                .map(v -> WalletOpResponseDTO
                        .builder()
                        .fromMemberId(v.getFirst().getWalletId())
                        .fromMemberBalance(v.getFirst().getAfterBalance())
                        .toMemberId(v.getSecond().getWalletId())
                        .toMemberBalance(v.getSecond().getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService transfer Err"));
    }
}
