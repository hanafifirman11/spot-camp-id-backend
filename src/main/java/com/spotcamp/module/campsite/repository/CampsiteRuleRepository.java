package com.spotcamp.module.campsite.repository;

import com.spotcamp.module.campsite.entity.CampsiteRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampsiteRuleRepository extends JpaRepository<CampsiteRule, Long> {

    /**
     * Find all rules for a campsite
     */
    List<CampsiteRule> findByCampsiteIdOrderByDisplayOrderAsc(Long campsiteId);

    /**
     * Delete all rules for a campsite
     */
    void deleteByCampsiteId(Long campsiteId);

    /**
     * Count rules for a campsite
     */
    long countByCampsiteId(Long campsiteId);
}
