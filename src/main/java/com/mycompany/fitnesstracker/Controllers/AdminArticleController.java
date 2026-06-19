package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminDTO;
import com.mycompany.fitnesstracker.Models.ArticleEntities.ArticleAdminRequest;
import com.mycompany.fitnesstracker.Services.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin management of Discover articles. ROLE_ADMIN is enforced in
 * ArticleService (service-layer check) — method security is not enabled here.
 */
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<List<ArticleAdminDTO>> list() {
        return ResponseEntity.ok(articleService.adminList());
    }

    @PostMapping
    public ResponseEntity<ArticleAdminDTO> create(@RequestBody ArticleAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.adminCreate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleAdminDTO> update(@PathVariable Long id,
                                                  @RequestBody ArticleAdminRequest request) {
        return ResponseEntity.ok(articleService.adminUpdate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
