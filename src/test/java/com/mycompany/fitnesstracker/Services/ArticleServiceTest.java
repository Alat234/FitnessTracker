package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Article;
import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminDTO;
import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminRequest;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverArticleCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverArticleDetailsDTO;
import com.mycompany.fitnesstracker.Models.Enums.ArticleCategory;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleServiceTest {

    private ArticleRepository articleRepository;
    private UserService userService;
    private ArticleService service;

    @BeforeEach
    void setUp() {
        articleRepository = mock(ArticleRepository.class);
        userService = mock(UserService.class);
        service = new ArticleService(articleRepository, userService);
    }

    private Article article(long id, String title, boolean published) {
        Article a = new Article();
        a.setId(id);
        a.setTitle(title);
        a.setSummary("Summary " + id);
        a.setContent("Content " + id);
        a.setCategory(ArticleCategory.TRAINING);
        a.setImageUrl("http://img/" + id + ".png");
        a.setPublished(published);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return a;
    }

    private void mockAdmin() {
        when(userService.getUserByJWt())
                .thenReturn(User.builder().id(1L).email("admin@test.com").role(Role.ROLE_ADMIN).build());
    }

    private void mockNonAdmin() {
        when(userService.getUserByJWt())
                .thenReturn(User.builder().id(2L).email("user@test.com").role(Role.ROLE_USER).build());
    }

    @Test
    void getPublishedArticles_returnsOnlyPublishedFromRepository() {
        when(articleRepository.findAllByPublishedTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(article(1, "Alpha", true), article(2, "Beta", true)));

        List<DiscoverArticleCardDTO> cards = service.getPublishedArticles();

        assertEquals(2, cards.size());
        assertEquals("Alpha", cards.get(0).title());
        verify(articleRepository).findAllByPublishedTrueOrderByCreatedAtDesc();
    }

    @Test
    void getPublishedArticle_returnsDetails_whenPublished() {
        when(articleRepository.findByIdAndPublishedTrue(5L)).thenReturn(Optional.of(article(5, "Gamma", true)));

        DiscoverArticleDetailsDTO dto = service.getPublishedArticle(5L);

        assertEquals("Gamma", dto.title());
        assertEquals("Content 5", dto.content());
    }

    @Test
    void getPublishedArticle_unpublishedOrMissing_throws404() {
        when(articleRepository.findByIdAndPublishedTrue(404L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> service.getPublishedArticle(404L));
    }

    @Test
    void adminCreate_savesAndReturnsDTO_forAdmin() {
        mockAdmin();
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });
        ArticleAdminRequest req = ArticleAdminRequest.builder()
                .title("New").summary("S").content("C")
                .category(ArticleCategory.NUTRITION).imageUrl("x").published(true).build();

        ArticleAdminDTO dto = service.adminCreate(req);

        assertEquals("New", dto.getTitle());
        assertEquals(ArticleCategory.NUTRITION, dto.getCategory());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void adminUpdate_appliesRequestToExisting_forAdmin() {
        mockAdmin();
        Article existing = article(3, "Old", false);
        when(articleRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));
        ArticleAdminRequest req = ArticleAdminRequest.builder()
                .title("Updated").summary("S2").content("C2")
                .category(ArticleCategory.RECOVERY).imageUrl("y").published(true).build();

        ArticleAdminDTO dto = service.adminUpdate(3L, req);

        assertEquals("Updated", dto.getTitle());
        assertTrue(dto.getPublished());
        verify(articleRepository).save(existing);
    }

    @Test
    void adminDelete_deletes_forAdmin() {
        mockAdmin();
        Article existing = article(4, "ToDelete", true);
        when(articleRepository.findById(4L)).thenReturn(Optional.of(existing));

        service.adminDelete(4L);

        verify(articleRepository).delete(existing);
    }

    @Test
    void adminList_forNonAdmin_throws403_andDoesNotQuery() {
        mockNonAdmin();

        assertThrows(BaseException.class, () -> service.adminList());
        verify(articleRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void adminCreate_forNonAdmin_throws403_andDoesNotSave() {
        mockNonAdmin();

        assertThrows(BaseException.class, () -> service.adminCreate(
                ArticleAdminRequest.builder().title("X").build()));
        verify(articleRepository, never()).save(any());
    }

    @Test
    void adminDelete_forNonAdmin_throws403_andDoesNotDelete() {
        mockNonAdmin();

        assertThrows(BaseException.class, () -> service.adminDelete(1L));
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void discoverArticleDtos_doNotExposeUnsafeFields() {
        for (Class<?> dto : List.of(DiscoverArticleCardDTO.class, DiscoverArticleDetailsDTO.class)) {
            assertFalse(hasComponentContaining(dto, "author"));
            assertFalse(hasComponentContaining(dto, "email"));
            assertFalse(hasComponentContaining(dto, "password"));
        }
    }

    private boolean hasComponentContaining(Class<?> recordClass, String needle) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch(n -> n.toLowerCase().contains(needle));
    }
}
