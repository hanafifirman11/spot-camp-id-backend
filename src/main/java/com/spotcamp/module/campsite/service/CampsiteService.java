package com.spotcamp.module.campsite.service;

import com.spotcamp.module.campsite.dto.*;
import com.spotcamp.module.campsite.entity.*;
import com.spotcamp.module.campsite.repository.*;
import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.common.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampsiteService {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteImageRepository imageRepository;
    private final CampsiteRuleRepository ruleRepository;
    private final AmenityRepository amenityRepository;
    private final ReviewRepository reviewRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.campsites-dir}")
    private String campsitesUploadDir;

    /**
     * List campsites for a merchant
     */
    public CampsiteListResponseDTO listMerchantCampsites(Long merchantId, CampsiteStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Campsite> campsites;
        if (status != null) {
            campsites = campsiteRepository.findByOwnerIdAndStatus(merchantId, status, pageable);
        } else {
            campsites = campsiteRepository.findByOwnerId(merchantId, pageable);
        }

        List<CampsiteResponseDTO> content = campsites.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return CampsiteListResponseDTO.of(content, page, size, campsites.getTotalElements());
    }

    /**
     * Get campsite by ID for merchant
     */
    public CampsiteDetailResponseDTO getCampsite(Long campsiteId, Long merchantId) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        return mapToDetailResponse(campsite);
    }

    /**
     * Create new campsite
     */
    @Transactional
    public CampsiteResponseDTO createCampsite(Long merchantId, CampsiteRequestDTO request) {
        log.info("Creating campsite for merchant: {}", merchantId);

        Campsite campsite = Campsite.builder()
                .ownerId(merchantId)
                .code(CodeGenerator.generateUnique("CMS", 8, campsiteRepository::existsByCode))
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getAddress())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .basePrice(request.getBasePrice())
                .checkInTime(request.getCheckInTime() != null ? request.getCheckInTime() : java.time.LocalTime.of(14, 0))
                .checkOutTime(request.getCheckOutTime() != null ? request.getCheckOutTime() : java.time.LocalTime.of(12, 0))
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .status(CampsiteStatus.ACTIVE)
                .build();

        campsite = campsiteRepository.save(campsite);
        log.info("Campsite created with ID: {}", campsite.getId());

        return mapToResponse(campsite);
    }

    /**
     * Update campsite
     */
    @Transactional
    public CampsiteResponseDTO updateCampsite(Long campsiteId, Long merchantId, CampsiteRequestDTO request) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        campsite.setName(request.getName());
        campsite.setDescription(request.getDescription());
        campsite.setLocation(request.getAddress());
        campsite.setAddress(request.getAddress());
        campsite.setLatitude(request.getLatitude());
        campsite.setLongitude(request.getLongitude());
        campsite.setBasePrice(request.getBasePrice());
        if (request.getCheckInTime() != null) {
            campsite.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            campsite.setCheckOutTime(request.getCheckOutTime());
        }
        campsite.setContactEmail(request.getContactEmail());
        campsite.setContactPhone(request.getContactPhone());

        campsite = campsiteRepository.save(campsite);
        log.info("Campsite updated: {}", campsiteId);

        return mapToResponse(campsite);
    }

    /**
     * Delete campsite (soft delete)
     */
    @Transactional
    public void deleteCampsite(Long campsiteId, Long merchantId) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        campsite.setStatus(CampsiteStatus.INACTIVE);
        campsiteRepository.save(campsite);
        log.info("Campsite soft deleted: {}", campsiteId);
    }

    /**
     * Update campsite status
     */
    @Transactional
    public CampsiteResponseDTO updateStatus(Long campsiteId, Long merchantId, CampsiteStatus status) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        if (status == CampsiteStatus.SUSPENDED) {
            throw new BusinessException("Merchants cannot set campsite status to SUSPENDED");
        }

        campsite.setStatus(status);
        campsite = campsiteRepository.save(campsite);
        log.info("Campsite status updated: {} -> {}", campsiteId, status);

        return mapToResponse(campsite);
    }

    /**
     * Get summary stats for merchant campsites
     */
    public CampsiteSummaryResponseDTO getMerchantSummary(Long merchantId) {
        long total = campsiteRepository.countByOwnerId(merchantId);
        long active = campsiteRepository.countByOwnerIdAndStatus(merchantId, CampsiteStatus.ACTIVE);
        long inactive = campsiteRepository.countByOwnerIdAndStatus(merchantId, CampsiteStatus.INACTIVE);
        long suspended = campsiteRepository.countByOwnerIdAndStatus(merchantId, CampsiteStatus.SUSPENDED);

        return CampsiteSummaryResponseDTO.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .suspended(suspended)
                .build();
    }

    /**
     * Add image to campsite
     */
    @Transactional
    public CampsiteImageResponseDTO addImage(Long campsiteId, Long merchantId, MultipartFile file, String caption, Boolean requestedPrimary) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        // Save file
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = "/api/v1/uploads/campsites/" + campsiteId + "/" + fileName;

        try {
            Path uploadPath = Paths.get(campsitesUploadDir, String.valueOf(campsiteId));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        } catch (IOException e) {
            throw new BusinessException("Failed to upload image: " + e.getMessage());
        }

        // Determine if this should be primary
        long imageCount = imageRepository.countByCampsiteId(campsiteId);
        boolean isPrimary = Boolean.TRUE.equals(requestedPrimary) || imageCount == 0;
        if (isPrimary) {
            imageRepository.resetPrimaryForCampsite(campsiteId);
        }

        CampsiteImage image = CampsiteImage.builder()
                .campsite(campsite)
                .imageUrl(fileUrl)
                .caption(caption)
                .displayOrder((int) imageCount)
                .isPrimary(isPrimary)
                .build();

        image = imageRepository.save(image);
        if (isPrimary) {
            campsite.setCoverImageUrl(image.getImageUrl());
            campsiteRepository.save(campsite);
        }
        log.info("Image added to campsite: {}", campsiteId);

        return CampsiteImageResponseDTO.builder()
                .id(image.getId())
                .url(image.getImageUrl())
                .caption(image.getCaption())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    /**
     * Set primary image for a campsite
     */
    @Transactional
    public CampsiteImageResponseDTO setPrimaryImage(Long campsiteId, Long merchantId, Long imageId) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        CampsiteImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("CampsiteImage", "id", imageId));

        if (!campsiteId.equals(image.getCampsiteId())) {
            throw new BusinessException("Image does not belong to campsite");
        }

        imageRepository.resetPrimaryForCampsite(campsiteId);
        image.setIsPrimary(true);
        image = imageRepository.save(image);

        campsite.setCoverImageUrl(image.getImageUrl());
        campsiteRepository.save(campsite);

        return CampsiteImageResponseDTO.builder()
                .id(image.getId())
                .url(image.getImageUrl())
                .caption(image.getCaption())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    /**
     * Update campsite amenities
     */
    @Transactional
    public AmenityResponseDTO updateAmenities(Long campsiteId, Long merchantId, List<Long> amenityIds) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        // Get amenities by IDs
        List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

        // Clear existing and add new
        campsite.getAmenities().clear();
        campsite.getAmenities().addAll(amenities);
        campsiteRepository.save(campsite);

        log.info("Amenities updated for campsite: {}", campsiteId);

        List<AmenityResponseDTO.AmenityItem> items = amenities.stream()
                .map(a -> AmenityResponseDTO.AmenityItem.builder()
                        .id(a.getId())
                        .code(a.getCode())
                        .name(a.getName())
                        .icon(a.getIcon())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        return AmenityResponseDTO.of(items);
    }

    /**
     * Update campsite rules
     */
    @Transactional
    public List<String> updateRules(Long campsiteId, Long merchantId, List<String> rules) {
        Campsite campsite = campsiteRepository.findByIdAndOwnerId(campsiteId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        // Delete existing rules
        ruleRepository.deleteByCampsiteId(campsiteId);

        // Add new rules
        for (int i = 0; i < rules.size(); i++) {
            CampsiteRule rule = CampsiteRule.builder()
                    .campsite(campsite)
                    .ruleText(rules.get(i))
                    .displayOrder(i)
                    .build();
            ruleRepository.save(rule);
        }

        log.info("Rules updated for campsite: {}", campsiteId);
        return rules;
    }

    /**
     * List all available amenities
     */
    public AmenityResponseDTO listAmenities() {
        List<Amenity> amenities = amenityRepository.findByIsActiveTrue();

        List<AmenityResponseDTO.AmenityItem> items = amenities.stream()
                .map(a -> AmenityResponseDTO.AmenityItem.builder()
                        .id(a.getId())
                        .code(a.getCode())
                        .name(a.getName())
                        .icon(a.getIcon())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        return AmenityResponseDTO.of(items);
    }

    private CampsiteResponseDTO mapToResponse(Campsite campsite) {
        return CampsiteResponseDTO.builder()
                .id(campsite.getId())
                .code(campsite.getCode())
                .name(campsite.getName())
                .description(campsite.getDescription())
                .address(campsite.getAddress())
                .latitude(campsite.getLatitude())
                .longitude(campsite.getLongitude())
                .basePrice(campsite.getBasePrice())
                .coverImageUrl(campsite.getCoverImageUrl())
                .checkInTime(campsite.getCheckInTime())
                .checkOutTime(campsite.getCheckOutTime())
                .contactEmail(campsite.getContactEmail())
                .contactPhone(campsite.getContactPhone())
                .status(campsite.getStatus())
                .rating(campsite.getRating() != null ? campsite.getRating().doubleValue() : 0.0)
                .reviewCount(campsite.getReviewCount())
                .createdAt(campsite.getCreatedAt())
                .updatedAt(campsite.getUpdatedAt())
                .build();
    }

    private CampsiteDetailResponseDTO mapToDetailResponse(Campsite campsite) {
        // Get images
        List<CampsiteDetailResponseDTO.CampsiteImageDTO> images = imageRepository
                .findByCampsiteIdOrderByDisplayOrderAsc(campsite.getId())
                .stream()
                .map(img -> CampsiteDetailResponseDTO.CampsiteImageDTO.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .caption(img.getCaption())
                        .isPrimary(img.getIsPrimary())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        // Get amenities
        List<CampsiteDetailResponseDTO.AmenityDTO> amenities = amenityRepository
                .findByCampsiteId(campsite.getId())
                .stream()
                .map(a -> CampsiteDetailResponseDTO.AmenityDTO.builder()
                        .id(a.getId())
                        .code(a.getCode())
                        .name(a.getName())
                        .icon(a.getIcon())
                        .category(a.getCategory())
                        .build())
                .collect(Collectors.toList());

        // Get rules
        List<String> rules = ruleRepository
                .findByCampsiteIdOrderByDisplayOrderAsc(campsite.getId())
                .stream()
                .map(CampsiteRule::getRuleText)
                .collect(Collectors.toList());

        return CampsiteDetailResponseDTO.builder()
                .id(campsite.getId())
                .code(campsite.getCode())
                .name(campsite.getName())
                .description(campsite.getDescription())
                .address(campsite.getAddress())
                .latitude(campsite.getLatitude())
                .longitude(campsite.getLongitude())
                .basePrice(campsite.getBasePrice())
                .coverImageUrl(campsite.getCoverImageUrl())
                .checkInTime(campsite.getCheckInTime())
                .checkOutTime(campsite.getCheckOutTime())
                .contactEmail(campsite.getContactEmail())
                .contactPhone(campsite.getContactPhone())
                .status(campsite.getStatus())
                .rating(campsite.getRating() != null ? campsite.getRating().doubleValue() : 0.0)
                .reviewCount(campsite.getReviewCount())
                .images(images)
                .amenities(amenities)
                .rules(rules)
                .createdAt(campsite.getCreatedAt())
                .updatedAt(campsite.getUpdatedAt())
                .build();
    }
}
