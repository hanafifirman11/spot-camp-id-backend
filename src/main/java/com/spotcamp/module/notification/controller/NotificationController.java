package com.spotcamp.module.notification.controller;

import com.spotcamp.security.AuthenticationFacade;
import com.spotcamp.module.notification.dto.NotificationResponseDTO;
import com.spotcamp.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticationFacade authenticationFacade;

    @GetMapping
    @Operation(summary = "Get my notifications")
    @PreAuthorize("hasAnyRole('CAMPER', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<NotificationResponseDTO>> listNotifications() {
        Long userId = authenticationFacade.getCurrentUserId();
        return ResponseEntity.ok(
                notificationService.listByUser(userId).stream()
                        .map(NotificationResponseDTO::from)
                        .toList()
        );
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    @PreAuthorize("hasAnyRole('CAMPER', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        Long userId = authenticationFacade.getCurrentUserId();
        return ResponseEntity.ok(NotificationResponseDTO.from(notificationService.markAsRead(userId, id)));
    }
}
