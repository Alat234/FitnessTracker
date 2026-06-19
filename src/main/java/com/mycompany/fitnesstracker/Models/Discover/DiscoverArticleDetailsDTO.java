package com.mycompany.fitnesstracker.Models.Discover;

import com.mycompany.fitnesstracker.Models.Enums.ArticleCategory;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Full article for the Discover detail page. Safe fields only — no author or
 * private data.
 */
@Builder
public record DiscoverArticleDetailsDTO(
        Long id,
        String title,
        String summary,
        String content,
        ArticleCategory category,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
