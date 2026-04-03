package com.spotcamp.module.payment.bank.account;

import com.spotcamp.module.payment.bank.account.dto.MerchantBankAccountRequestDTO;
import com.spotcamp.module.payment.bank.account.dto.MerchantBankAccountResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MerchantBankAccountController {

    private final MerchantBankAccountService bankAccountService;

    @GetMapping("/merchant/bank-accounts")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<MerchantBankAccountResponseDTO>> listMyAccounts() {
        return ResponseEntity.ok(bankAccountService.listMyAccounts());
    }

    @PostMapping("/merchant/bank-accounts")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<MerchantBankAccountResponseDTO> createAccount(
        @Valid @RequestBody MerchantBankAccountRequestDTO request
    ) {
        return ResponseEntity.ok(bankAccountService.createAccount(request));
    }

    @PutMapping("/merchant/bank-accounts/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<MerchantBankAccountResponseDTO> updateAccount(
        @PathVariable Long id,
        @Valid @RequestBody MerchantBankAccountRequestDTO request
    ) {
        return ResponseEntity.ok(bankAccountService.updateAccount(id, request));
    }

    @DeleteMapping("/merchant/bank-accounts/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        bankAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/campsites/{campsiteId}/bank-accounts")
    public ResponseEntity<List<MerchantBankAccountResponseDTO>> listCampsiteBankAccounts(
        @PathVariable Long campsiteId
    ) {
        return ResponseEntity.ok(bankAccountService.listActiveForCampsite(campsiteId));
    }
}
