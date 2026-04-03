package com.spotcamp.visualmap.domain;

/**
 * Status of a map configuration
 */
public enum MapStatus {
    /**
     * Configuration is being edited and not yet published
     */
    DRAFT,
    
    /**
     * Configuration is live and being used
     */
    ACTIVE,
    
    /**
     * Configuration is no longer in use but kept for history
     */
    ARCHIVED
}