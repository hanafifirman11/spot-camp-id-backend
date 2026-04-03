package com.spotcamp.module.payment.bank.account;

import com.spotcamp.module.campsite.repository.CampsiteRepository;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.security.AuthenticationFacade;
import com.spotcamp.module.payment.bank.BankCode;
import com.spotcamp.module.payment.bank.account.dto.MerchantBankAccountRequestDTO;
import com.spotcamp.module.payment.bank.account.dto.MerchantBankAccountResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantBankAccountService {

    private final MerchantBankAccountRepository bankAccountRepository;
    private final CampsiteRepository campsiteRepository;
    private final AuthenticationFacade authenticationFacade;

    @Transactional(readOnly = true)
    public List<MerchantBankAccountResponseDTO> listMyAccounts() {
        Long merchantId = authenticationFacade.getCurrentUserId();
        return bankAccountRepository.findByMerchantId(merchantId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MerchantBankAccountResponseDTO createAccount(MerchantBankAccountRequestDTO request) {
        Long merchantId = authenticationFacade.getCurrentUserId();
        MerchantBankAccount account = MerchantBankAccount.builder()
            .merchantId(merchantId)
            .bankCode(request.getBankCode())
            .accountName(request.getAccountName())
            .accountNumber(request.getAccountNumber())
            .isActive(Boolean.TRUE.equals(request.getIsActive()))
            .build();
        return toResponse(bankAccountRepository.save(account));
    }

    @Transactional
    public MerchantBankAccountResponseDTO updateAccount(Long id, MerchantBankAccountRequestDTO request) {
        MerchantBankAccount account = bankAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank account", id));
        ensureOwner(account.getMerchantId());
        account.setBankCode(request.getBankCode());
        account.setAccountName(request.getAccountName());
        account.setAccountNumber(request.getAccountNumber());
        account.setActive(Boolean.TRUE.equals(request.getIsActive()));
        return toResponse(bankAccountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long id) {
        MerchantBankAccount account = bankAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bank account", id));
        ensureOwner(account.getMerchantId());
        bankAccountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public List<MerchantBankAccountResponseDTO> listActiveForCampsite(Long campsiteId) {
        Long merchantId = campsiteRepository.findById(campsiteId)
            .map(campsite -> campsite.getOwnerId())
            .orElseThrow(() -> new ResourceNotFoundException("Campsite", campsiteId));
        return bankAccountRepository.findByMerchantIdAndIsActiveTrue(merchantId).stream()
            .map(this::toResponse)
            .toList();
    }

    private void ensureOwner(Long merchantId) {
        Long currentUser = authenticationFacade.getCurrentUserId();
        if (!merchantId.equals(currentUser)) {
            throw new ResourceNotFoundException("Bank account", null);
        }
    }

    private MerchantBankAccountResponseDTO toResponse(MerchantBankAccount account) {
        String bankName = account.getBankCode();
        try {
            bankName = BankCode.valueOf(account.getBankCode()).getDisplayName();
        } catch (Exception ignored) {
        }
        return MerchantBankAccountResponseDTO.builder()
            .id(account.getId())
            .bankCode(account.getBankCode())
            .bankName(bankName)
            .accountName(account.getAccountName())
            .accountNumber(account.getAccountNumber())
            .isActive(account.isActive())
            .build();
    }
}
