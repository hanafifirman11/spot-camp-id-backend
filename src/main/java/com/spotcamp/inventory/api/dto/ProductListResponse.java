package com.spotcamp.inventory.api.dto;

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
public class ProductListResponse {

    private List<ProductResponse> content;
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

    public static ProductListResponse of(List<ProductResponse> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return ProductListResponse.builder()
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
