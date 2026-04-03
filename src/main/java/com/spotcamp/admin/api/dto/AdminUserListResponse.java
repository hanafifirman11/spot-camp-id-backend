package com.spotcamp.admin.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {
    private List<AdminUserSummaryResponse> content;
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

    public static AdminUserListResponse of(List<AdminUserSummaryResponse> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return AdminUserListResponse.builder()
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
