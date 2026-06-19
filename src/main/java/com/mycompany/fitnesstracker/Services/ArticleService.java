package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Article;
import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminDTO;
import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminRequest;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverArticleCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverArticleDetailsDTO;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.ArticleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Articles / Tips module. Authenticated read of published articles plus
 * admin-only CRUD. Role gating is done here (service layer) — this project does
 * not enable method security, mirroring DiscoverService / GymService.
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserService userService;

    /* ── Discover read (authenticated, published only) ── */

    @Transactional
    public List<DiscoverArticleCardDTO> getPublishedArticles() {
        return articleRepository.findAllByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::toCard)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscoverArticleDetailsDTO getPublishedArticle(Long id) {
        Article article = articleRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new BaseException("Article not found", HttpStatus.NOT_FOUND));
        return toDetails(article);
    }

    /* ── Admin CRUD (ROLE_ADMIN only) ── */

    @Transactional
    public List<ArticleAdminDTO> adminList() {
        requireAdmin();
        return articleRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleAdminDTO adminCreate(ArticleAdminRequest request) {
        requireAdmin();
        Article article = new Article();
        apply(article, request);
        return toAdminDTO(articleRepository.save(article));
    }

    @Transactional
    public ArticleAdminDTO adminUpdate(Long id, ArticleAdminRequest request) {
        requireAdmin();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BaseException("Article not found", HttpStatus.NOT_FOUND));
        apply(article, request);
        return toAdminDTO(articleRepository.save(article));
    }

    @Transactional
    public void adminDelete(Long id) {
        requireAdmin();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BaseException("Article not found", HttpStatus.NOT_FOUND));
        articleRepository.delete(article);
    }

    /* ── helpers ── */

    private void requireAdmin() {
        User user = userService.getUserByJWt();
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BaseException("Admin access required", HttpStatus.FORBIDDEN);
        }
    }

    private void apply(Article article, ArticleAdminRequest request) {
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setImageUrl(request.getImageUrl());
        article.setPublished(request.getPublished());
    }

    private DiscoverArticleCardDTO toCard(Article a) {
        return DiscoverArticleCardDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .category(a.getCategory())
                .imageUrl(a.getImageUrl())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private DiscoverArticleDetailsDTO toDetails(Article a) {
        return DiscoverArticleDetailsDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .content(a.getContent())
                .category(a.getCategory())
                .imageUrl(a.getImageUrl())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private ArticleAdminDTO toAdminDTO(Article a) {
        return ArticleAdminDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .content(a.getContent())
                .category(a.getCategory())
                .imageUrl(a.getImageUrl())
                .published(a.getPublished())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
