package com.spotcamp.common.init;

import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.module.campsite.entity.Campsite;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
import com.spotcamp.common.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeBackfillRunner implements ApplicationRunner {

    private static final int CODE_LENGTH = 8;

    private final UserRepository userRepository;
    private final CampsiteRepository campsiteRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        backfillBusinessCodes();
        backfillCampsiteCodes();
    }

    private void backfillBusinessCodes() {
        List<User> merchants = userRepository.findByRole(UserRole.MERCHANT);
        if (merchants.isEmpty()) {
            return;
        }

        Set<String> existingCodes = new HashSet<>();
        for (User user : merchants) {
            if (hasText(user.getBusinessCode())) {
                existingCodes.add(user.getBusinessCode());
            }
        }

        List<User> updates = new ArrayList<>();
        for (User user : merchants) {
            if (hasText(user.getBusinessCode())) {
                continue;
            }
            String code = CodeGenerator.generateUnique(
                    "BSN",
                    CODE_LENGTH,
                    candidate -> existingCodes.contains(candidate) || userRepository.existsByBusinessCode(candidate)
            );
            existingCodes.add(code);
            user.setBusinessCode(code);
            updates.add(user);
        }

        if (!updates.isEmpty()) {
            userRepository.saveAll(updates);
            log.info("Generated business codes for {} merchants", updates.size());
        }
    }

    private void backfillCampsiteCodes() {
        List<Campsite> campsites = campsiteRepository.findAll();
        if (campsites.isEmpty()) {
            return;
        }

        Set<String> existingCodes = new HashSet<>();
        for (Campsite campsite : campsites) {
            if (hasText(campsite.getCode())) {
                existingCodes.add(campsite.getCode());
            }
        }

        List<Campsite> updates = new ArrayList<>();
        for (Campsite campsite : campsites) {
            if (hasText(campsite.getCode())) {
                continue;
            }
            String code = CodeGenerator.generateUnique(
                    "CMS",
                    CODE_LENGTH,
                    candidate -> existingCodes.contains(candidate) || campsiteRepository.existsByCode(candidate)
            );
            existingCodes.add(code);
            campsite.setCode(code);
            updates.add(campsite);
        }

        if (!updates.isEmpty()) {
            campsiteRepository.saveAll(updates);
            log.info("Generated campsite codes for {} campsites", updates.size());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
