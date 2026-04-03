package com.spotcamp.authuser.events;

import com.spotcamp.common.event.DomainEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Event fired when a user successfully logs in
 */
@Data
@Builder
public class UserLoginEvent implements DomainEvent {

    private final String eventType = "user.login";
    private final LocalDateTime occurredOn;
    private final String aggregateId;
    private final Long userId;
    private final String email;
    private final String ipAddress;
    private final String userAgent;

    public static UserLoginEvent create(Long userId, String email, String ipAddress, String userAgent) {
        return UserLoginEvent.builder()
                .occurredOn(LocalDateTime.now())
                .aggregateId(userId.toString())
                .userId(userId)
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }
}