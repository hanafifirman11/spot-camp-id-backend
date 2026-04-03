package com.spotcamp.campsite.domain;

/**
 * Status enum for campsites
 */
public enum CampsiteStatus {
    /**
     * Campsite is active and visible to customers
     */
    ACTIVE,

    /**
     * Campsite is temporarily inactive (hidden from search)
     */
    INACTIVE,

    /**
     * Campsite is suspended by admin
     */
    SUSPENDED
}
