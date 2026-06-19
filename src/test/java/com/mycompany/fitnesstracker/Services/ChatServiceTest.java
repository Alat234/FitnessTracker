package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Chat.ChatMessageDTO;
import com.mycompany.fitnesstracker.Models.Chat.ChatPartnerDTO;
import com.mycompany.fitnesstracker.Models.ChatMessage;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.ChatMessageRepository;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class ChatServiceTest {

    private ChatMessageRepository chatMessageRepository;
    private UserConnectionRepository connectionRepository;
    private UserRepository userRepository;
    private UserService userService;
    private ChatService service;

    private final User trainer = User.builder().id(1L).email("t@test.com").role(Role.ROLE_TRAINER).build();
    private final User clientUser = User.builder().id(2L).email("c@test.com").role(Role.ROLE_USER).build();
    private final User admin = User.builder().id(3L).email("admin@test.com").role(Role.ROLE_ADMIN).build();
    private final User otherUser = User.builder().id(4L).email("o@test.com").role(Role.ROLE_USER).build();

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        connectionRepository = mock(UserConnectionRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        service = new ChatService(chatMessageRepository, connectionRepository, userRepository, userService);
        // default: no connection between any pair
        when(connectionRepository.findByOwnerAndViewerAndType(any(User.class), any(User.class), any(ConnectionType.class)))
                .thenReturn(Optional.empty());
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            if (m.getId() == null) m.setId(99L);
            return m;
        });
    }

    /** Accepted TRAINER connection with owner=client, viewer=trainer. */
    private void mockAcceptedTrainerConnection(User client, User trainerUser) {
        UserConnection conn = mock(UserConnection.class);
        when(conn.getStatus()).thenReturn(ConnectionStatus.ACCEPTED);
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainerUser, ConnectionType.TRAINER))
                .thenReturn(Optional.of(conn));
    }

    /* ── Trainer-client (existing behavior) ── */

    @Test
    void trainer_sendsToAcceptedClient() {
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(clientUser));
        mockAcceptedTrainerConnection(clientUser, trainer);

        ChatMessageDTO dto = service.send(2L, "  hi  ");

        assertEquals("hi", dto.text());
        assertTrue(dto.fromMe());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void client_sendsToAcceptedTrainer() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(trainer));
        mockAcceptedTrainerConnection(clientUser, trainer);

        ChatMessageDTO dto = service.send(1L, "yo coach");

        assertEquals("yo coach", dto.text());
        assertTrue(dto.fromMe());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void trainer_cannotSendToNonClient() {
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(clientUser));

        assertThrows(BaseException.class, () -> service.send(2L, "hi"));
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void history_returnsCanonicalPairMessages() {
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(clientUser));
        mockAcceptedTrainerConnection(clientUser, trainer);
        ChatMessage m = new ChatMessage();
        m.setId(1L); m.setText("hey"); m.setUserLow(trainer); m.setUserHigh(clientUser); m.setSender(trainer);
        when(chatMessageRepository.findByUserLowAndUserHighOrderByCreatedAtAsc(trainer, clientUser))
                .thenReturn(List.of(m));

        List<ChatMessageDTO> msgs = service.getMessages(2L);

        assertEquals(1, msgs.size());
        assertTrue(msgs.get(0).fromMe());
        verify(chatMessageRepository).findByUserLowAndUserHighOrderByCreatedAtAsc(trainer, clientUser);
    }

    /* ── Admin support ── */

    @Test
    void user_sendsToAdmin() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        when(userRepository.findUserById(3L)).thenReturn(Optional.of(admin));

        ChatMessageDTO dto = service.send(3L, "need help");

        assertEquals("need help", dto.text());
        assertTrue(dto.fromMe());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void admin_repliesToUser() {
        when(userService.getUserByJWt()).thenReturn(admin);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(clientUser));

        ChatMessageDTO dto = service.send(2L, "how can I help");

        assertEquals("how can I help", dto.text());
        assertTrue(dto.fromMe());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void trainer_sendsToAdmin() {
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(3L)).thenReturn(Optional.of(admin));

        ChatMessageDTO dto = service.send(3L, "support please");

        assertEquals("support please", dto.text());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void admin_readsOnlyOwnThreadWithPartner_notArbitraryPrivateChat() {
        // Admin reads conversation with clientUser → repo is queried with the (admin,client) pair only.
        when(userService.getUserByJWt()).thenReturn(admin);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(clientUser));
        // canonical: client id 2 < admin id 3 → low=client, high=admin
        when(chatMessageRepository.findByUserLowAndUserHighOrderByCreatedAtAsc(clientUser, admin))
                .thenReturn(List.of());

        service.getMessages(2L);

        verify(chatMessageRepository).findByUserLowAndUserHighOrderByCreatedAtAsc(clientUser, admin);
    }

    /* ── Negatives / validation ── */

    @Test
    void unrelatedNonAdminUsers_cannotChat() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        when(userRepository.findUserById(4L)).thenReturn(Optional.of(otherUser));

        assertThrows(BaseException.class, () -> service.getMessages(4L));
        assertThrows(BaseException.class, () -> service.send(4L, "hi"));
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void blankMessage_fails() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        when(userRepository.findUserById(3L)).thenReturn(Optional.of(admin));

        assertThrows(BaseException.class, () -> service.send(3L, "   "));
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void tooLongMessage_fails() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        when(userRepository.findUserById(3L)).thenReturn(Optional.of(admin));

        assertThrows(BaseException.class, () -> service.send(3L, "x".repeat(1001)));
        verify(chatMessageRepository, never()).save(any());
    }

    /* ── Partner lists ── */

    @Test
    void userPartners_includeAcceptedTrainerAndAdminSupport() {
        when(userService.getUserByJWt()).thenReturn(clientUser);
        UserConnection conn = mock(UserConnection.class);
        when(conn.getViewer()).thenReturn(trainer);
        when(connectionRepository.findAllByOwnerAndTypeAndStatus(clientUser, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(conn));
        when(userRepository.findAllByRole(Role.ROLE_ADMIN)).thenReturn(List.of(admin));

        List<ChatPartnerDTO> partners = service.getPartners();

        assertEquals(2, partners.size());
        ChatPartnerDTO trainerPartner = partners.stream().filter(p -> p.id().equals(1L)).findFirst().orElseThrow();
        ChatPartnerDTO adminPartner = partners.stream().filter(p -> p.id().equals(3L)).findFirst().orElseThrow();
        assertFalse(trainerPartner.support());
        assertTrue(adminPartner.support());
    }

    @Test
    void adminPartners_onlyUsersWithExistingSupportThreads() {
        when(userService.getUserByJWt()).thenReturn(admin);
        ChatMessage m = new ChatMessage();
        m.setId(1L); m.setUserLow(clientUser); m.setUserHigh(admin); m.setSender(clientUser); m.setText("hi");
        when(chatMessageRepository.findAllByUserLowOrUserHigh(admin, admin)).thenReturn(List.of(m));

        List<ChatPartnerDTO> partners = service.getPartners();

        assertEquals(1, partners.size());
        assertEquals(2L, partners.get(0).id());
        assertTrue(partners.get(0).support());
    }
}
