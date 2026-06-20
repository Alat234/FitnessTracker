package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.NotificationType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Notification;
import com.mycompany.fitnesstracker.Models.Notifications.NotificationDTO;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Repositories.NotificationRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * In-app notifications (MVP, REST + polling — no WebSocket). Personal events
 * ({@code system=false}) are emitted by other services via {@link #notify}; admin
 * announcements ({@code system=true}) are fanned out per recipient. Read access is
 * always scoped to the current user; the admin broadcast is service-layer gated
 * (this project does not enable method security — mirrors ArticleService).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /* ── Emit (called by other services; never blocks the core action) ── */

    /**
     * Create a personal notification for {@code recipient}. No-op if recipient is
     * null or is the actor themselves (don't notify someone about their own action).
     */
    @Transactional
    public void notify(User recipient, NotificationType type, String title, String message,
                       String entityType, Long entityId, User actor) {
        if (recipient == null) return;
        if (actor != null && actor.getId().equals(recipient.getId())) return;

        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setEntityType(entityType);
        n.setEntityId(entityId);
        n.setRead(false);
        n.setSystem(false);
        notificationRepository.save(n);
    }

    /* ── Admin broadcast (ROLE_ADMIN only) ── */

    /** Fan a system announcement out to all users or to one target role. Returns recipient count. */
    @Transactional
    public int createAnnouncement(String title, String message, Role targetRole) {
        requireAdmin();
        if (title == null || title.trim().isEmpty()) {
            throw new BaseException("Title is required", HttpStatus.BAD_REQUEST);
        }
        List<User> recipients = (targetRole == null)
                ? userRepository.findAll()
                : userRepository.findAllByRole(targetRole);

        String cleanTitle = title.trim();
        for (User user : recipients) {
            Notification n = new Notification();
            n.setRecipient(user);
            n.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
            n.setTitle(cleanTitle);
            n.setMessage(message);
            n.setRead(false);
            n.setSystem(true);
            notificationRepository.save(n);
        }
        return recipients.size();
    }

    /* ── Read side (current user only) ── */

    @Transactional
    public List<NotificationDTO> listMy(boolean unreadOnly) {
        User me = userService.getUserByJWt();
        List<Notification> list = unreadOnly
                ? notificationRepository.findAllByRecipientAndReadFalseOrderByCreatedAtDesc(me)
                : notificationRepository.findAllByRecipientOrderByCreatedAtDesc(me);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public long unreadCount() {
        return notificationRepository.countByRecipientAndReadFalse(userService.getUserByJWt());
    }

    @Transactional
    public void markRead(Long id) {
        User me = userService.getUserByJWt();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new BaseException("Notification not found", HttpStatus.NOT_FOUND));
        if (!n.getRecipient().getId().equals(me.getId())) {
            throw new BaseException("This notification does not belong to you", HttpStatus.FORBIDDEN);
        }
        if (!n.isRead()) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAllRead() {
        User me = userService.getUserByJWt();
        List<Notification> unread = notificationRepository.findAllByRecipientAndReadFalseOrderByCreatedAtDesc(me);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /* ── helpers ── */

    private void requireAdmin() {
        User user = userService.getUserByJWt();
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BaseException("Admin access required", HttpStatus.FORBIDDEN);
        }
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .isRead(n.isRead())
                .system(n.isSystem())
                .actorName(actorName(n.getActor()))
                .createdAt(n.getCreatedAt())
                .build();
    }

    /** Display name of the actor (full name, falling back to email); null when no actor. */
    private String actorName(User user) {
        if (user == null) return null;
        UserInfo info = user.getUserInfo();
        String name = info == null ? null
                : Stream.of(info.getFirstName(), info.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));
        return (name == null || name.isBlank()) ? user.getEmail() : name;
    }
}
