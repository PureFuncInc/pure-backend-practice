package net.purefunc.practice.wallet;

import net.purefunc.practice.member.MemberRepository;
import net.purefunc.practice.wallet.data.WalletOperationType;
import net.purefunc.practice.wallet.data.WalletResponseDTO;
import net.purefunc.practice.wallet.data.WalletTransactionPO;
import net.purefunc.practice.wallet.data.WalletVO;
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

    public Page<WalletVO> getTransactions(String name, Integer page, Integer size) {
        return memberRepository
                .findByUsername(name)
                .map(v -> walletTransactionRepository.findAllTxRecords(v.getId(), PageRequest.of(page, size)))
                .orElseThrow(() -> new RuntimeException("WalletService getTransactions Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    WalletResponseDTO deposit(
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
                                            .memo(WalletOperationType.DEPOSIT.name())
                                            .build()
                            );
                        }))
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(v.getWalletId())
                        .fromBalance(v.getBeforeBalance())
                        .toId(v.getWalletId())
                        .toBalance(v.getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService deposit Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    WalletResponseDTO withdraw(
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
                                            .memo(WalletOperationType.WITHDRAW.name())
                                            .build()
                            );
                        }))
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(v.getWalletId())
                        .fromBalance(v.getBeforeBalance())
                        .toId(v.getWalletId())
                        .toBalance(v.getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService deposit Err"));
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    WalletResponseDTO transfer(
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

                                    final WalletTransactionPO walletTransactionPO1 = walletTransactionRepository.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w1.getId())
                                                    .operationUuid(UUID.randomUUID().toString())
                                                    .operationType(WalletOperationType.TRANSFER_OUT)
                                                    .beforeBalance(beforeBalanceW1)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW1)
                                                    .memo(WalletOperationType.TRANSFER_OUT.name())
                                                    .build()
                                    );

                                    final WalletTransactionPO walletTransactionPO2 = walletTransactionRepository.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w2.getId())
                                                    .operationUuid(UUID.randomUUID().toString())
                                                    .operationType(WalletOperationType.TRANSFER_IN)
                                                    .beforeBalance(beforeBalanceW2)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW2)
                                                    .memo(WalletOperationType.TRANSFER_IN.name())
                                                    .build()
                                    );

                                    return Pair.of(walletTransactionPO1, walletTransactionPO2);
                                }))
                )
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(v.getFirst().getWalletId())
                        .fromBalance(v.getFirst().getAfterBalance())
                        .toId(v.getSecond().getWalletId())
                        .toBalance(v.getSecond().getAfterBalance())
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("WalletService transfer Err"));
    }
}
