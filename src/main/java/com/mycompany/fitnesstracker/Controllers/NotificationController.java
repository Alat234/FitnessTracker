package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Notifications.NotificationDTO;
import com.mycompany.fitnesstracker.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Current user's in-app notifications. Everything is scoped to the caller in
 * NotificationService; no extra access config needed (covered by
 * anyRequest().authenticated()).
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** My notifications, newest first. {@code ?unread=true} returns unread only. */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> list(
            @RequestParam(value = "unread", required = false, defaultValue = "false") boolean unread) {
        return ResponseEntity.ok(notificationService.listMy(unread));
    }

    /** Unread count for the bell badge. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.noContent().build();
    }
}
