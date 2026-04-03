package com.spotcamp.module.authuser.event;

import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.common.event.DomainEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Event fired when a new user is registered
 */
@Data
@Builder
public class UserRegisteredEvent implements DomainEvent {

    private final String eventType = "user.registered";
    private final LocalDateTime occurredOn;
    private final String aggregateId;
    private final Long userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    private final String businessName;

    public static UserRegisteredEvent create(Long userId, String email, String firstName, 
                                           String lastName, UserRole role, String businessName) {
        return UserRegisteredEvent.builder()
                .occurredOn(LocalDateTime.now())
                .aggregateId(userId.toString())
                .userId(userId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .businessName(businessName)
                .build();
    }
}