package com.spotcamp.common.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events
 * Events represent something significant that happened in the domain
 */
public interface DomainEvent {
    
    /**
     * Gets the timestamp when the event occurred
     */
    LocalDateTime getOccurredOn();
    
    /**
     * Gets the event type identifier
     */
    String getEventType();
    
    /**
     * Gets the aggregate ID associated with this event
     */
    String getAggregateId();
    
    /**
     * Gets the version of the event schema
     */
    default int getEventVersion() {
        return 1;
    }
}