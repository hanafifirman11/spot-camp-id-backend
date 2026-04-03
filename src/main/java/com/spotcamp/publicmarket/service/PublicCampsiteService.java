package com.spotcamp.publicmarket.service;

import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.repository.UserRepository;
import com.spotcamp.campsite.domain.Campsite;
import com.spotcamp.campsite.domain.CampsiteImage;
import com.spotcamp.campsite.domain.CampsiteRule;
import com.spotcamp.campsite.domain.CampsiteStatus;
import com.spotcamp.campsite.repository.AmenityRepository;
import com.spotcamp.campsite.repository.CampsiteImageRepository;
import com.spotcamp.campsite.repository.CampsiteRepository;
import com.spotcamp.campsite.repository.CampsiteRuleRepository;
import com.spotcamp.campsite.repository.ReviewRepository;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.publicmarket.api.dto.CampsitePublicDetail;
import com.spotcamp.publicmarket.api.dto.CampsitePublicResponse;
import com.spotcamp.publicmarket.api.dto.MapConfigPublicResponse;
import com.spotcamp.visualmap.domain.MapConfiguration;
import com.spotcamp.visualmap.service.MapConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCampsiteService {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteImageRepository imageRepository;
    private final CampsiteRuleRepository ruleRepository;
    private final AmenityRepository amenityRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MapConfigurationService mapConfigService;

    /**
     * Search and list active campsites for public marketplace
     */
    public Page<CampsitePublicResponse> searchCampsites(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("rating").descending());

        Page<Campsite> campsites;
        if (query != null && !query.trim().isEmpty()) {
            campsites = campsiteRepository.searchByQuery(query.trim(), CampsiteStatus.ACTIVE, pageable);
        } else {
            campsites = campsiteRepository.findByStatus(CampsiteStatus.ACTIVE, pageable);
        }

        return campsites.map(this::mapToPublicResponse);
    }

    /**
     * Get public campsite detail
     */
    public CampsitePublicDetail getCampsiteDetail(Long campsiteId) {
        Campsite campsite = campsiteRepository.findByIdAndStatus(campsiteId, CampsiteStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        return mapToPublicDetail(campsite);
    }

    /**
     * Get map configuration for public view
     */
    public MapConfigPublicResponse getMapConfig(Long campsiteId) {
        return getMapConfig(campsiteId, null, null);
    }

    /**
     * Get map configuration for public view by map code or id
     */
    public MapConfigPublicResponse getMapConfig(Long campsiteId, String mapCode, Long mapId) {
        // Verify campsite exists and is active
        campsiteRepository.findByIdAndStatus(campsiteId, CampsiteStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));

        try {
            MapConfiguration config;
            if (mapCode != null && !mapCode.trim().isEmpty()) {
                config = mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode);
            } else if (mapId != null) {
                config = mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId);
            } else {
                config = mapConfigService.getActiveConfiguration(campsiteId);
            }
            return MapConfigPublicResponse.builder()
                    .id(config.getId())
                    .campsiteId(config.getCampsiteId())
                    .mapCode(config.getMapCode())
                    .mapName(config.getMapName())
                    .imageWidth(config.getImageWidth())
                    .imageHeight(config.getImageHeight())
                    .backgroundImageUrl(config.getBackgroundImageUrl())
                    .spots(config.getConfigData().getSpots())
                    .build();
        } catch (ResourceNotFoundException e) {
            // Map config not found is acceptable, return empty response
            return null;
        }
    }

    /**
     * Get active map configurations for public view
     */
    public List<MapConfiguration> getActiveMapConfigs(Long campsiteId) {
        campsiteRepository.findByIdAndStatus(campsiteId, CampsiteStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", "id", campsiteId));
        return mapConfigService.getActiveConfigurations(campsiteId);
    }

    private CampsitePublicResponse mapToPublicResponse(Campsite campsite) {
        // Get primary image
        String imageUrl = imageRepository.findByCampsiteIdAndIsPrimaryTrue(campsite.getId())
                .map(CampsiteImage::getImageUrl)
                .orElse("assets/placeholder-camp.jpg");

        // Get amenities
        List<String> amenities = amenityRepository.findByCampsiteId(campsite.getId())
                .stream()
                .map(a -> a.getName())
                .collect(Collectors.toList());

        // Get rating and review count
        Double rating = reviewRepository.calculateAverageRating(campsite.getId());
        long reviewCount = reviewRepository.countByCampsite_IdAndStatus(
                campsite.getId(),
                com.spotcamp.campsite.domain.ReviewStatus.ACTIVE
        );

        return CampsitePublicResponse.builder()
                .id(campsite.getId())
                .name(campsite.getName())
                .description(campsite.getDescription())
                .latitude(campsite.getLatitude())
                .longitude(campsite.getLongitude())
                .price(campsite.getBasePrice())
                .image(imageUrl)
                .rating(rating != null ? rating : 0.0)
                .reviews((int) reviewCount)
                .amenities(amenities)
                .build();
    }

    private CampsitePublicDetail mapToPublicDetail(Campsite campsite) {
        // Get images
        List<CampsitePublicDetail.CampsiteImageDto> images = imageRepository
                .findByCampsiteIdOrderByDisplayOrderAsc(campsite.getId())
                .stream()
                .map(img -> CampsitePublicDetail.CampsiteImageDto.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .caption(img.getCaption())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        // Get amenities
        List<String> amenities = amenityRepository.findByCampsiteId(campsite.getId())
                .stream()
                .map(a -> a.getName())
                .collect(Collectors.toList());

        // Get rules
        List<String> rules = ruleRepository.findByCampsiteIdOrderByDisplayOrderAsc(campsite.getId())
                .stream()
                .map(CampsiteRule::getRuleText)
                .collect(Collectors.toList());

        // Get merchant info
        User merchant = userRepository.findById(campsite.getOwnerId())
                .orElse(null);

        CampsitePublicDetail.MerchantInfo merchantInfo = null;
        if (merchant != null) {
            merchantInfo = CampsitePublicDetail.MerchantInfo.builder()
                    .name(merchant.getBusinessName() != null ? merchant.getBusinessName()
                            : merchant.getFirstName() + " " + merchant.getLastName())
                    .joinedAt(merchant.getCreatedAt().toLocalDate())
                    .build();
        }

        // Get rating and review count
        Double rating = reviewRepository.calculateAverageRating(campsite.getId());
        long reviewCount = reviewRepository.countByCampsite_IdAndStatus(
                campsite.getId(),
                com.spotcamp.campsite.domain.ReviewStatus.ACTIVE
        );

        // Get primary image for main display
        String primaryImage = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(CampsitePublicDetail.CampsiteImageDto::getUrl)
                .orElse(images.isEmpty() ? "assets/placeholder-camp.jpg" : images.get(0).getUrl());

        return CampsitePublicDetail.builder()
                .id(campsite.getId())
                .name(campsite.getName())
                .description(campsite.getDescription())
                .latitude(campsite.getLatitude())
                .longitude(campsite.getLongitude())
                .price(campsite.getBasePrice())
                .image(primaryImage)
                .rating(rating != null ? rating : 0.0)
                .reviews((int) reviewCount)
                .amenities(amenities)
                .rules(rules)
                .merchant(merchantInfo)
                .images(images)
                .build();
    }
}
