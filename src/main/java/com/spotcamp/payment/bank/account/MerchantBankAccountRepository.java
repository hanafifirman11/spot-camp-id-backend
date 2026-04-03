package com.spotcamp.payment.bank.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantBankAccountRepository extends JpaRepository<MerchantBankAccount, Long> {
    List<MerchantBankAccount> findByMerchantId(Long merchantId);
    List<MerchantBankAccount> findByMerchantIdAndIsActiveTrue(Long merchantId);
    List<MerchantBankAccount> findByMerchantIdAndBankCodeIgnoreCaseAndIsActiveTrue(Long merchantId, String bankCode);
}
