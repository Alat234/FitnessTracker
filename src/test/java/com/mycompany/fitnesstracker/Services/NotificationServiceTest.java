package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.NotificationType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Notification;
import com.mycompany.fitnesstracker.Models.Notifications.NotificationDTO;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.NotificationRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private UserService userService;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        service = new NotificationService(notificationRepository, userRepository, userService);
    }

    private User user(long id, Role role) {
        return User.builder().id(id).email("u" + id + "@test.com").role(role).build();
    }

    private Notification notif(long id, User recipient, boolean read) {
        Notification n = new Notification();
        n.setId(id);
        n.setRecipient(recipient);
        n.setType(NotificationType.CHAT_MESSAGE);
        n.setTitle("t");
        n.setRead(read);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    private void mockMe(User me) {
        when(userService.getUserByJWt()).thenReturn(me);
    }

    /* ── notify (emit) ── */

    @Test
    void notify_savesPersonalRow_forRecipient() {
        User recipient = user(5, Role.ROLE_USER);
        User actor = user(1, Role.ROLE_TRAINER);

        service.notify(recipient, NotificationType.APPOINTMENT_ASSIGNED,
                "Workout scheduled", "Leg day · Jun 20, 10:00", "APPOINTMENT", 9L, actor);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(recipient, saved.getRecipient());
        assertEquals(actor, saved.getActor());
        assertEquals(NotificationType.APPOINTMENT_ASSIGNED, saved.getType());
        assertEquals("APPOINTMENT", saved.getEntityType());
        assertEquals(9L, saved.getEntityId());
        assertFalse(saved.isRead());
        assertFalse(saved.isSystem());
    }

    @Test
    void notify_skipsWhenRecipientNull() {
        service.notify(null, NotificationType.CHAT_MESSAGE, "x", "y", "CHAT", 1L, user(1, Role.ROLE_USER));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void notify_skipsWhenActorIsRecipient() {
        User me = user(7, Role.ROLE_USER);
        service.notify(me, NotificationType.CHAT_MESSAGE, "x", "y", "CHAT", 1L, me);
        verify(notificationRepository, never()).save(any());
    }

    /* ── admin announcements ── */

    @Test
    void createAnnouncement_forAdmin_fansOutToAllUsers() {
        mockMe(user(1, Role.ROLE_ADMIN));
        when(userRepository.findAll()).thenReturn(List.of(
                user(2, Role.ROLE_USER), user(3, Role.ROLE_TRAINER), user(4, Role.ROLE_USER)));

        int count = service.createAnnouncement("Maintenance", "Down at 2am", null);

        assertEquals(3, count);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(3)).save(captor.capture());
        Notification any = captor.getValue();
        assertTrue(any.isSystem());
        assertEquals(NotificationType.SYSTEM_ANNOUNCEMENT, any.getType());
        verify(userRepository, never()).findAllByRole(any());
    }

    @Test
    void createAnnouncement_withTargetRole_usesFindAllByRole() {
        mockMe(user(1, Role.ROLE_ADMIN));
        when(userRepository.findAllByRole(Role.ROLE_TRAINER))
                .thenReturn(List.of(user(2, Role.ROLE_TRAINER), user(3, Role.ROLE_TRAINER)));

        int count = service.createAnnouncement("Trainers only", "Note", Role.ROLE_TRAINER);

        assertEquals(2, count);
        verify(notificationRepository, times(2)).save(any());
        verify(userRepository, never()).findAll();
    }

    @Test
    void createAnnouncement_forNonAdmin_throws403_andDoesNotSave() {
        mockMe(user(2, Role.ROLE_USER));
        assertThrows(BaseException.class,
                () -> service.createAnnouncement("X", "Y", null));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createAnnouncement_blankTitle_throws400() {
        mockMe(user(1, Role.ROLE_ADMIN));
        assertThrows(BaseException.class,
                () -> service.createAnnouncement("   ", "Y", null));
        verify(notificationRepository, never()).save(any());
    }

    /* ── read side ── */

    @Test
    void listMy_all_returnsMappedDtos() {
        User me = user(2, Role.ROLE_USER);
        mockMe(me);
        when(notificationRepository.findAllByRecipientOrderByCreatedAtDesc(me))
                .thenReturn(List.of(notif(1, me, false), notif(2, me, true)));

        List<NotificationDTO> dtos = service.listMy(false);

        assertEquals(2, dtos.size());
        assertFalse(dtos.get(0).isRead());
        assertTrue(dtos.get(1).isRead());
        verify(notificationRepository).findAllByRecipientOrderByCreatedAtDesc(me);
    }

    @Test
    void listMy_unreadOnly_usesUnreadQuery() {
        User me = user(2, Role.ROLE_USER);
        mockMe(me);
        when(notificationRepository.findAllByRecipientAndReadFalseOrderByCreatedAtDesc(me))
                .thenReturn(List.of(notif(1, me, false)));

        List<NotificationDTO> dtos = service.listMy(true);

        assertEquals(1, dtos.size());
        verify(notificationRepository).findAllByRecipientAndReadFalseOrderByCreatedAtDesc(me);
        verify(notificationRepository, never()).findAllByRecipientOrderByCreatedAtDesc(any());
    }

    @Test
    void unreadCount_returnsRepoCount() {
        User me = user(2, Role.ROLE_USER);
        mockMe(me);
        when(notificationRepository.countByRecipientAndReadFalse(me)).thenReturn(4L);

        assertEquals(4L, service.unreadCount());
    }

    @Test
    void markRead_marksAndSaves_whenOwner() {
        User me = user(2, Role.ROLE_USER);
        mockMe(me);
        Notification n = notif(7, me, false);
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(n));

        service.markRead(7L);

        assertTrue(n.isRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markRead_throws404_whenMissing() {
        mockMe(user(2, Role.ROLE_USER));
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> service.markRead(404L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markRead_throws403_whenNotOwner() {
        mockMe(user(2, Role.ROLE_USER));
        Notification someoneElses = notif(8, user(99, Role.ROLE_USER), false);
        when(notificationRepository.findById(8L)).thenReturn(Optional.of(someoneElses));

        assertThrows(BaseException.class, () -> service.markRead(8L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllRead_marksAllUnreadAndSaves() {
        User me = user(2, Role.ROLE_USER);
        mockMe(me);
        Notification a = notif(1, me, false);
        Notification b = notif(2, me, false);
        when(notificationRepository.findAllByRecipientAndReadFalseOrderByCreatedAtDesc(me))
                .thenReturn(List.of(a, b));

        service.markAllRead();

        assertTrue(a.isRead());
        assertTrue(b.isRead());
        verify(notificationRepository).saveAll(List.of(a, b));
    }
}
