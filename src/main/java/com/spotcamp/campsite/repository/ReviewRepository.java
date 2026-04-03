package com.spotcamp.campsite.repository;

import com.spotcamp.campsite.domain.Review;
import com.spotcamp.campsite.domain.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find approved reviews for a campsite
     */
    Page<Review> findByCampsite_IdAndStatus(Long campsiteId, ReviewStatus status, Pageable pageable);

    /**
     * Find review by user and campsite
     */
    Optional<Review> findByUserIdAndCampsite_Id(Long userId, Long campsiteId);

    /**
     * Calculate average rating for a campsite
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.campsite.id = :campsiteId AND r.status = 'ACTIVE'")
    Double calculateAverageRating(@Param("campsiteId") Long campsiteId);

    /**
     * Count approved reviews for a campsite
     */
    long countByCampsite_IdAndStatus(Long campsiteId, ReviewStatus status);

    /**
     * Check if user already reviewed this campsite
     */
    boolean existsByUserIdAndCampsite_Id(Long userId, Long campsiteId);
}
