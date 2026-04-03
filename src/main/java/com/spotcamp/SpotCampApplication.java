package com.spotcamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Spot Camp ID Backend
 * 
 * Campsite Booking & Management Platform
 * Features: Visual spot selection, hybrid inventory, real-time booking
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class SpotCampApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotCampApplication.class, args);
    }
}
