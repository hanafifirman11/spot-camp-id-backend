package com.spotcamp.module.admin.theme.controller;

import com.spotcamp.module.admin.theme.entity.Theme;
import com.spotcamp.module.admin.theme.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "Admin theme switching and public theme resolution")
public class ThemeController {

    private final ThemeService themeService;

    @Operation(summary = "Get active theme (Public)", description = "Returns the currently active theme configuration for the frontend")
    @GetMapping("/public/theme/active")
    public ResponseEntity<Theme> getActiveTheme() {
        return ResponseEntity.ok(themeService.getActiveTheme());
    }

    @Operation(summary = "List all themes (Admin)", description = "List available themes")
    @GetMapping("/admin/themes")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<Theme>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @Operation(summary = "Activate a theme (Admin)", description = "Sets a specific theme as active and deactivates others")
    @PostMapping("/admin/themes/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Theme> activateTheme(@PathVariable Long id) {
        return ResponseEntity.ok(themeService.activateTheme(id));
    }
    
    @Operation(summary = "Get theme details (Admin)")
    @GetMapping("/admin/themes/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Theme> getTheme(@PathVariable Long id) {
        return ResponseEntity.ok(themeService.getTheme(id));
    }
}
