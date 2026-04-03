package com.spotcamp.module.admin.theme.service;

import com.spotcamp.module.admin.theme.entity.Theme;
import com.spotcamp.module.admin.theme.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private static final String DEFAULT_THEME_CODE = "default";
    private static final String DEFAULT_THEME_NAME = "SpotCamp Forest";
    private static final String DEFAULT_THEME_DESCRIPTION = "Default SpotCamp nature theme";
    private static final String DEFAULT_THEME_TOKENS = """
        {
          "--primary-green": "#2E5C48",
          "--secondary-green": "#5C8D74",
          "--accent-sand": "#E6B984",
          "--accent-rust": "#D47E56",
          "--text-dark": "#2C3333",
          "--text-light": "#F0F2EE",
          "--bg-nature": "#F7F9F6",
          "--white": "#FFFFFF",
          "--shadow-soft": "0 4px 20px rgba(46, 92, 72, 0.08)",
          "--shadow-card": "0 8px 30px rgba(0, 0, 0, 0.05)",
          "--radius-lg": "16px",
          "--radius-md": "12px",
          "--radius-sm": "8px",
          "--primary": "#2E5C48",
          "--secondary": "#5C8D74",
          "--background": "#F7F9F6"
        }
        """;

    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme getActiveTheme() {
        Optional<Theme> activeTheme = themeRepository.findByIsActiveTrue();
        if (activeTheme.isPresent()) {
            return activeTheme.get();
        }

        Optional<Theme> defaultTheme = themeRepository.findByCode(DEFAULT_THEME_CODE);
        if (defaultTheme.isPresent()) {
            Theme theme = defaultTheme.get();
            themeRepository.deactivateAll();
            theme.setActive(true);
            return themeRepository.save(theme);
        }

        Optional<Theme> firstTheme = themeRepository.findAll(PageRequest.of(0, 1, Sort.by("id"))).stream().findFirst();
        if (firstTheme.isPresent()) {
            Theme theme = firstTheme.get();
            themeRepository.deactivateAll();
            theme.setActive(true);
            return themeRepository.save(theme);
        }

        Theme created = Theme.builder()
                .code(DEFAULT_THEME_CODE)
                .name(DEFAULT_THEME_NAME)
                .description(DEFAULT_THEME_DESCRIPTION)
                .tokensJson(DEFAULT_THEME_TOKENS)
                .isActive(true)
                .build();
        return themeRepository.save(created);
    }

    @Transactional
    public Theme activateTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        themeRepository.deactivateAll();
        theme.setActive(true);
        return themeRepository.save(theme);
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found"));
    }
}
