package net.purefunc.practice.wallet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Tag(name = "💰 Wallet")
@RestController
@RequestMapping("/api/v1.0")
@SecurityRequirement(name = "Authentication")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "cc")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets/transactions")
    ResponseEntity<List<String>> getWalletsTransactions(Principal principal) {
        return Optional
                .of(walletService.getTransactions(principal.getName()))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("getWalletsTransactions Err"));
    }

    @Operation(summary = "cc")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:deposit")
    ResponseEntity<WalletResponseDTO> deposit(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .deposit(
                        principal.getName(),
                        walletRequestDTO.toId,
                        walletRequestDTO.amount
                )
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("deposit Err"));
    }

    @Operation(summary = "cc")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:withdraw")
    ResponseEntity<WalletResponseDTO> withdraw(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .withdraw(
                        principal.getName(),
                        walletRequestDTO.fromId,
                        walletRequestDTO.amount
                )
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("withdraw Err"));
    }

    @Operation(summary = "cc")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wallets:transfer")
    ResponseEntity<WalletResponseDTO> transfer(
            @RequestBody WalletRequestDTO walletRequestDTO,
            Principal principal) {
        return walletService
                .transfer(
                        principal.getName(),
                        walletRequestDTO.fromId,
                        walletRequestDTO.toId,
                        walletRequestDTO.amount
                )
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("transfer Err"));
    }

    // LOCK TYPE
}
