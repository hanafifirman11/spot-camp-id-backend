package com.spotcamp.module.inventory.dto;

import java.util.List;

public class BundleAvailabilityResult {
    private boolean available;
    private String message;
    private List<UnavailableComponent> unavailableComponents;

    public static BundleAvailabilityResult available() {
        BundleAvailabilityResult result = new BundleAvailabilityResult();
        result.available = true;
        return result;
    }

    public static BundleAvailabilityResult unavailable(String message) {
        BundleAvailabilityResult result = new BundleAvailabilityResult();
        result.available = false;
        result.message = message;
        return result;
    }

    public static BundleAvailabilityResult unavailable(List<UnavailableComponent> components) {
        BundleAvailabilityResult result = new BundleAvailabilityResult();
        result.available = false;
        result.unavailableComponents = components;
        return result;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }

    public List<UnavailableComponent> getUnavailableComponents() {
        return unavailableComponents;
    }
}
