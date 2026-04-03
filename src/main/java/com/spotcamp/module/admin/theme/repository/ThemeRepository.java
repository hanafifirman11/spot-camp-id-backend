package com.spotcamp.module.admin.theme.repository;

import com.spotcamp.module.admin.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    
    Optional<Theme> findByCode(String code);
    
    Optional<Theme> findByIsActiveTrue();

    @Modifying
    @Query("UPDATE Theme t SET t.isActive = false")
    void deactivateAll();
}
