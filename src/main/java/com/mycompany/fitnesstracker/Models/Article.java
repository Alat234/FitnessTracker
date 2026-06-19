package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.ArticleCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Discover educational content (fitness tips, nutrition, training advice,
 * platform announcements). Authenticated read; admin-managed. Image is a URL
 * string only — no upload pipeline.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @Column(name = "article_title")
    private String title;

    @Column(name = "article_summary")
    private String summary;

    @Column(name = "article_content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_category")
    private ArticleCategory category;

    @Column(name = "article_image_url")
    private String imageUrl;

    @Column(name = "article_published")
    private Boolean published;

    @Column(name = "article_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "article_updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (published == null) published = false;
        if (category == null) category = ArticleCategory.GENERAL;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
