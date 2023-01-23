package net.purefunc.practice.wallet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.purefunc.practice.wallet.data.WalletRequestDTO;
import net.purefunc.practice.wallet.data.WalletResponseDTO;
import net.purefunc.practice.wallet.data.WalletTransferRequestDTO;
import net.purefunc.practice.wallet.data.WalletVO;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "💰 Wallet")
@RestController
@RequestMapping("/api/v1.0")
@SecurityRequirement(name = "Authentication")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "查詢交易紀錄")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets/transactions")
    Page<WalletVO> getWalletsTransactions(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Principal principal) {
        return walletService.getTransactions(principal.getName(), page, size);
    }

    @Operation(summary = "存款")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:deposit")
    WalletResponseDTO deposit(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .deposit(
                        principal.getName(),
                        walletRequestDTO.getAmount()
                );
    }

    @Operation(summary = "提款")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:withdraw")
    WalletResponseDTO withdraw(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .withdraw(
                        principal.getName(),
                        walletRequestDTO.getAmount()
                );
    }

    @Operation(summary = "轉帳")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:transfer")
    WalletResponseDTO transfer(
            @RequestBody WalletTransferRequestDTO walletTransferRequestDTO,
            Principal principal) {
        return walletService
                .transfer(
                        principal.getName(),
                        walletTransferRequestDTO.getToId(),
                        walletTransferRequestDTO.getAmount()
                );
    }

    // LOCK TYPE
}
