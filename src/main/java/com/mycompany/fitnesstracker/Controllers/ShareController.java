package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Connection.SharedProgressDTO;
import com.mycompany.fitnesstracker.Services.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    /** Прогрес користувача {ownerId} — лише для viewer-а з ACCEPTED-зв'язком. */
    @GetMapping("/{ownerId}/progress")
    public ResponseEntity<SharedProgressDTO> getSharedProgress(@PathVariable Long ownerId) {
        String email = currentUserEmail();
        return ResponseEntity.ok(shareService.getSharedProgress(ownerId, email));
    }

    /* ── helpers ──────────────────────────────────────────── */
    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
