package com.spotcamp.module.payment.bank;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/banks")
public class BankCatalogController {

    @GetMapping
    public ResponseEntity<List<BankOption>> listBanks() {
        List<BankOption> options = BankCode.list().stream()
            .map(code -> new BankOption(code.name(), code.getDisplayName()))
            .toList();
        return ResponseEntity.ok(options);
    }

    @Data
    @AllArgsConstructor
    public static class BankOption {
        private String code;
        private String name;
    }
}
