package net.purefunc.practice.wallet;

import net.purefunc.practice.member.MemberDAO;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private final MemberDAO memberDAO;
    private final WalletDAO walletDAO;
    private final WalletTransactionDAO walletTransactionDAO;

    public WalletService(MemberDAO memberDAO, WalletDAO walletDAO, WalletTransactionDAO walletTransactionDAO) {
        this.memberDAO = memberDAO;
        this.walletDAO = walletDAO;
        this.walletTransactionDAO = walletTransactionDAO;
    }

    public List<String> getTransactions(String name) {
        return null;
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    Optional<WalletResponseDTO> deposit(
            String username,
            Long toId,
            BigDecimal amount) {
        return Optional.of(
                        memberDAO
                                .findByUsername(username)
                                .filter(v -> v.getId().equals(toId))
                                .orElseThrow(() -> new RuntimeException("deposit username mapping member != toId"))
                )
                .flatMap(v -> walletDAO
                        .findByMemberId(toId)
                        .map(w -> {
                            final BigDecimal beforeBalance = w.balance;
                            final BigDecimal afterBalance = w.balance.add(amount);
                            w.balance = afterBalance;
                            walletDAO.save(w);

                            return walletTransactionDAO.save(
                                    WalletTransactionPO
                                            .builder()
                                            .id(null)
                                            .walletId(w.id)
                                            .operationType(OperationType.DEPOSIT)
                                            .beforeBalance(beforeBalance)
                                            .amount(amount)
                                            .afterBalance(afterBalance)
                                            .memo(OperationType.DEPOSIT.name())
                                            .build()
                            );
                        }))
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(toId)
                        .fromBalance(v.beforeBalance)
                        .toId(toId)
                        .toBalance(v.afterBalance)
                        .build());
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    Optional<WalletResponseDTO> withdraw(
            String username,
            Long fromId,
            BigDecimal amount) {
        return Optional.of(
                        memberDAO
                                .findByUsername(username)
                                .filter(v -> v.getId().equals(fromId))
                                .orElseThrow(() -> new RuntimeException("withdraw username mapping member != fromId"))
                )
                .flatMap(v -> walletDAO
                        .findByMemberId(fromId)
                        .map(w -> {
                            final BigDecimal beforeBalance = w.balance;
                            final BigDecimal afterBalance = w.balance.subtract(amount);
                            w.balance = afterBalance;
                            walletDAO.save(w);

                            return walletTransactionDAO.save(
                                    WalletTransactionPO
                                            .builder()
                                            .id(null)
                                            .walletId(w.id)
                                            .operationType(OperationType.WITHDRAW)
                                            .beforeBalance(beforeBalance)
                                            .amount(amount)
                                            .afterBalance(afterBalance)
                                            .memo(OperationType.WITHDRAW.name())
                                            .build()
                            );
                        }))
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(fromId)
                        .fromBalance(v.beforeBalance)
                        .toId(fromId)
                        .toBalance(v.afterBalance)
                        .build());
    }

    @Transactional(rollbackOn = {RuntimeException.class})
    Optional<WalletResponseDTO> transfer(
            String username,
            Long fromId,
            Long toId,
            BigDecimal amount) {
        return Optional.of(
                        memberDAO
                                .findByUsername(username)
                                .filter(v -> v.getId().equals(fromId))
                                .orElseThrow(() -> new RuntimeException("transfer username mapping member != fromId"))
                )
                .flatMap(v -> walletDAO
                        .findByMemberId(fromId)
                        .flatMap(w1 -> walletDAO
                                .findByMemberId(toId)
                                .map(w2 -> {
                                    final BigDecimal beforeBalanceW1 = w1.balance;
                                    final BigDecimal afterBalanceW1 = w1.balance.subtract(amount);
                                    w1.balance = afterBalanceW1;
                                    walletDAO.save(w1);

                                    final BigDecimal beforeBalanceW2 = w2.balance;
                                    final BigDecimal afterBalanceW2 = w2.balance.add(amount);
                                    w2.balance = afterBalanceW2;
                                    walletDAO.save(w2);

                                    final WalletTransactionPO walletTransactionPO1 = walletTransactionDAO.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w1.id)
                                                    .operationType(OperationType.TRANSFER_OUT)
                                                    .beforeBalance(beforeBalanceW1)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW1)
                                                    .memo(OperationType.TRANSFER_OUT.name())
                                                    .build()
                                    );

                                    final WalletTransactionPO walletTransactionPO2 = walletTransactionDAO.save(
                                            WalletTransactionPO
                                                    .builder()
                                                    .id(null)
                                                    .walletId(w2.id)
                                                    .operationType(OperationType.TRANSFER_IN)
                                                    .beforeBalance(beforeBalanceW2)
                                                    .amount(amount)
                                                    .afterBalance(afterBalanceW2)
                                                    .memo(OperationType.TRANSFER_IN.name())
                                                    .build()
                                    );

                                    return Pair.of(walletTransactionPO1, walletTransactionPO2);
                                }))
                )
                .map(v -> WalletResponseDTO
                        .builder()
                        .fromId(v.getFirst().walletId)
                        .fromBalance(v.getFirst().afterBalance)
                        .toId(v.getSecond().walletId)
                        .toBalance(v.getSecond().afterBalance)
                        .build()
                );
    }
}
