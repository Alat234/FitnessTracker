package com.mycompany.fitnesstracker.Models.Discover;

import com.mycompany.fitnesstracker.Models.Enums.ArticleCategory;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Article summary for the Discover articles grid. Safe fields only — no author
 * or private data.
 */
@Builder
public record DiscoverArticleCardDTO(
        Long id,
        String title,
        String summary,
        ArticleCategory category,
        String imageUrl,
        LocalDateTime createdAt
) {
}
