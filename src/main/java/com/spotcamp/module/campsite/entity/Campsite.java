package com.spotcamp.module.campsite.entity;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Campsite entity representing a camping location managed by a merchant
 */
@Entity
@Table(name = "campsites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campsite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "code", length = 20, unique = true)
    private String code;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "location", nullable = false, length = 500)
    private String location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime = LocalTime.of(14, 0);

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime = LocalTime.of(12, 0);

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CampsiteStatus status = CampsiteStatus.ACTIVE;

    @OneToMany(mappedBy = "campsite", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CampsiteImage> images = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "campsite_amenities",
            joinColumns = @JoinColumn(name = "campsite_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "campsite", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CampsiteRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "campsite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * Checks if campsite is active
     */
    public boolean isActive() {
        return status == CampsiteStatus.ACTIVE;
    }

    /**
     * Checks if campsite has coordinates
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Adds an image to this campsite
     */
    public void addImage(String imageUrl, String caption, int displayOrder) {
        CampsiteImage image = CampsiteImage.builder()
                .campsite(this)
                .imageUrl(imageUrl)
                .caption(caption)
                .displayOrder(displayOrder)
                .build();
        images.add(image);
    }

    /**
     * Adds an amenity to this campsite
     */
    public void addAmenity(Amenity amenity) {
        amenities.add(amenity);
    }

    /**
     * Removes an amenity from this campsite
     */
    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
    }

    /**
     * Adds a rule to this campsite
     */
    public void addRule(String ruleText, int displayOrder) {
        CampsiteRule rule = CampsiteRule.builder()
                .campsite(this)
                .ruleText(ruleText)
                .displayOrder(displayOrder)
                .build();
        rules.add(rule);
    }

    /**
     * Updates rating based on reviews
     */
    public void updateRating(BigDecimal newRating, int totalReviews) {
        this.rating = newRating;
        this.reviewCount = totalReviews;
    }
}
