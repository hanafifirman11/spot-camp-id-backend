package com.spotcamp.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for pagination operations
 */
public final class PageUtils {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
        // Utility class
    }

    /**
     * Creates a Pageable with safe defaults
     */
    public static Pageable createPageable(Integer page, Integer size, String sort) {
        int safePage = page != null ? Math.max(0, page) : 0;
        int safeSize = size != null ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        
        if (sort != null && !sort.isEmpty()) {
            return PageRequest.of(safePage, safeSize, parseSort(sort));
        }
        
        return PageRequest.of(safePage, safeSize);
    }

    /**
     * Parses sort string in format "field,direction"
     */
    private static Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(parts[0]).ascending();
        }
        
        String field = parts[0].trim();
        String direction = parts[1].trim();
        
        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.by(field).descending();
        }
        
        return Sort.by(field).ascending();
    }
}