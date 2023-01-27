package net.purefunc.practice.wallet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.purefunc.practice.wallet.data.dto.WalletOpResponseDTO;
import net.purefunc.practice.wallet.data.dto.WalletRequestDTO;
import net.purefunc.practice.wallet.data.dto.WalletResponseDTO;
import net.purefunc.practice.wallet.data.dto.WalletTransferRequestDTO;
import net.purefunc.practice.wallet.data.vo.WalletVO;
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

    @Operation(summary = "查詢餘額")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/wallets")
    WalletResponseDTO getWallets(Principal principal) {
        return walletService.query(principal.getName());
    }

    @Operation(summary = "存款")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:deposit")
    WalletOpResponseDTO deposit(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .deposit(
                        principal.getName(),
                        walletRequestDTO.getAmount()
                );
    }

    @Operation(summary = "轉帳")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:transfer")
    WalletOpResponseDTO transfer(
            @RequestBody WalletTransferRequestDTO walletTransferRequestDTO,
            Principal principal) {
        return walletService
                .transfer(
                        principal.getName(),
                        walletTransferRequestDTO.getToMemberId(),
                        walletTransferRequestDTO.getAmount()
                );
    }

    @Operation(summary = "提款")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:withdraw")
    WalletOpResponseDTO withdraw(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .withdraw(
                        principal.getName(),
                        walletRequestDTO.getAmount()
                );
    }

    @Operation(summary = "查詢交易紀錄")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/wallets/transactions")
    Page<WalletVO> getWalletsTransactions(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Principal principal) {
        return walletService.queryTransactions(principal.getName(), page, size);
    }

    // LOCK TYPE
}
