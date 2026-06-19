package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    /** Discover list: published only, newest first. */
    List<Article> findAllByPublishedTrueOrderByCreatedAtDesc();

    /** Discover detail: published only (unpublished/missing → empty). */
    Optional<Article> findByIdAndPublishedTrue(Long id);

    /** Admin list: everything, newest first. */
    List<Article> findAllByOrderByCreatedAtDesc();
}
