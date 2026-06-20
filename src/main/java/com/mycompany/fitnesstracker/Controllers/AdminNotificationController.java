package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Notifications.CreateAnnouncementRequest;
import com.mycompany.fitnesstracker.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin-created system announcements. ROLE_ADMIN is enforced in
 * NotificationService (service-layer check) — method security is not enabled here.
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    /** Broadcast a system announcement; {@code targetRole} null → all users. */
    @PostMapping
    public ResponseEntity<Map<String, Integer>> announce(@RequestBody CreateAnnouncementRequest request) {
        int recipients = notificationService.createAnnouncement(
                request.getTitle(), request.getMessage(), request.getTargetRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("recipients", recipients));
    }
}
