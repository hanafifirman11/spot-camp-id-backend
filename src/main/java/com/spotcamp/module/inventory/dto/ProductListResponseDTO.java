package com.spotcamp.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product list response with pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDTO {

    private List<ProductResponseDTO> content;
    private PageMetadata page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static ProductListResponseDTO of(List<ProductResponseDTO> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return ProductListResponseDTO.builder()
                .content(content)
                .page(PageMetadata.builder()
                        .number(page)
                        .size(size)
                        .totalElements(total)
                        .totalPages(totalPages)
                        .build())
                .build();
    }
}
