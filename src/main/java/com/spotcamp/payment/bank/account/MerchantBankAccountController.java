package com.spotcamp.payment.bank.account;

import com.spotcamp.payment.bank.account.dto.MerchantBankAccountRequest;
import com.spotcamp.payment.bank.account.dto.MerchantBankAccountResponse;
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
    public ResponseEntity<List<MerchantBankAccountResponse>> listMyAccounts() {
        return ResponseEntity.ok(bankAccountService.listMyAccounts());
    }

    @PostMapping("/merchant/bank-accounts")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<MerchantBankAccountResponse> createAccount(
        @Valid @RequestBody MerchantBankAccountRequest request
    ) {
        return ResponseEntity.ok(bankAccountService.createAccount(request));
    }

    @PutMapping("/merchant/bank-accounts/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<MerchantBankAccountResponse> updateAccount(
        @PathVariable Long id,
        @Valid @RequestBody MerchantBankAccountRequest request
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
    public ResponseEntity<List<MerchantBankAccountResponse>> listCampsiteBankAccounts(
        @PathVariable Long campsiteId
    ) {
        return ResponseEntity.ok(bankAccountService.listActiveForCampsite(campsiteId));
    }
}
