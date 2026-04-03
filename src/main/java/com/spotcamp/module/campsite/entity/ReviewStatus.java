package com.spotcamp.module.campsite.entity;

/**
 * Status enum for reviews
 */
public enum ReviewStatus {
    /**
     * Review is active and visible
     */
    ACTIVE,

    /**
     * Review is hidden by moderator
     */
    HIDDEN,

    /**
     * Review is soft deleted
     */
    DELETED
}
