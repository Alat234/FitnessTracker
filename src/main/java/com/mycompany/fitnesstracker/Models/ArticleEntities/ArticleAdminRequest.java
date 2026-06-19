package com.mycompany.fitnesstracker.Models.ArticleEntities;

import com.mycompany.fitnesstracker.Models.Enums.ArticleCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin create / update payload for an article.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleAdminRequest {
    private String title;
    private String summary;
    private String content;
    private ArticleCategory category;
    private String imageUrl;
    private Boolean published;
}
