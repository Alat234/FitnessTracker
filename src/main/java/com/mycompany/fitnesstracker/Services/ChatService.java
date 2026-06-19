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
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Repositories.ChatMessageRepository;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trainer-client + admin-support text chat (MVP, no real-time). Participants are
 * stored as a canonical (userLow, userHigh) pair. A conversation is allowed when
 * an accepted TRAINER connection exists between the two, or when exactly one side
 * is ROLE_ADMIN (support). Role gating is service-layer.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_LENGTH = 1000;

    private final ChatMessageRepository      chatMessageRepository;
    private final UserConnectionRepository   connectionRepository;
    private final UserRepository             userRepository;
    private final UserService                userService;

    /* ── Partners ──────────────────────────────────────────── */
    @Transactional
    public List<ChatPartnerDTO> getPartners() {
        User me = userService.getUserByJWt();
        Map<Long, ChatPartnerDTO> partners = new LinkedHashMap<>();

        if (me.getRole() == Role.ROLE_TRAINER) {
            connectionRepository
                    .findAllByViewerAndTypeAndStatus(me, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED)
                    .stream().map(UserConnection::getOwner)
                    .forEach(u -> partners.putIfAbsent(u.getId(), toPartner(u, false)));
            addAdmins(partners);
        } else if (me.getRole() == Role.ROLE_USER) {
            connectionRepository
                    .findAllByOwnerAndTypeAndStatus(me, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED)
                    .stream().map(UserConnection::getViewer)
                    .forEach(u -> partners.putIfAbsent(u.getId(), toPartner(u, false)));
            addAdmins(partners);
        } else if (me.getRole() == Role.ROLE_ADMIN) {
            // Only users who already have a support thread with this admin.
            chatMessageRepository.findAllByUserLowOrUserHigh(me, me).forEach(m -> {
                User other = m.getUserLow() != null && m.getUserLow().getId().equals(me.getId())
                        ? m.getUserHigh() : m.getUserLow();
                if (other != null) partners.putIfAbsent(other.getId(), toPartner(other, true));
            });
        } else {
            throw new BaseException("Chat is not available for this account", HttpStatus.FORBIDDEN);
        }
        return new ArrayList<>(partners.values());
    }

    private void addAdmins(Map<Long, ChatPartnerDTO> partners) {
        userRepository.findAllByRole(Role.ROLE_ADMIN)
                .forEach(a -> partners.putIfAbsent(a.getId(), toPartner(a, true)));
    }

    /* ── History ───────────────────────────────────────────── */
    @Transactional
    public List<ChatMessageDTO> getMessages(Long partnerId) {
        User me = userService.getUserByJWt();
        User partner = requireAccess(me, partnerId);
        User low = lowOf(me, partner);
        User high = highOf(me, partner);
        return chatMessageRepository
                .findByUserLowAndUserHighOrderByCreatedAtAsc(low, high)
                .stream().map(m -> toDTO(m, me)).collect(Collectors.toList());
    }

    /* ── Send ──────────────────────────────────────────────── */
    @Transactional
    public ChatMessageDTO send(Long partnerId, String rawText) {
        User me = userService.getUserByJWt();
        User partner = requireAccess(me, partnerId);

        String text = rawText == null ? "" : rawText.trim();
        if (text.isEmpty()) {
            throw new BaseException("Message cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (text.length() > MAX_LENGTH) {
            throw new BaseException("Message is too long (max " + MAX_LENGTH + " characters)", HttpStatus.BAD_REQUEST);
        }

        ChatMessage msg = new ChatMessage();
        msg.setUserLow(lowOf(me, partner));
        msg.setUserHigh(highOf(me, partner));
        msg.setSender(me);
        msg.setText(text);

        return toDTO(chatMessageRepository.save(msg), me);
    }

    /* ── access / helpers ──────────────────────────────────── */

    /** Loads the partner and verifies the current user may chat with them. */
    private User requireAccess(User me, Long partnerId) {
        User partner = userRepository.findUserById(partnerId)
                .orElseThrow(() -> new BaseException("Conversation not found", HttpStatus.NOT_FOUND));
        if (!isAllowed(me, partner)) {
            throw new BaseException("No chat access to this user", HttpStatus.FORBIDDEN);
        }
        return partner;
    }

    /** Allowed if accepted TRAINER connection (either direction) or exactly one side is ROLE_ADMIN. */
    private boolean isAllowed(User me, User partner) {
        if (me.getId().equals(partner.getId())) return false;
        boolean exactlyOneAdmin = (me.getRole() == Role.ROLE_ADMIN) ^ (partner.getRole() == Role.ROLE_ADMIN);
        if (exactlyOneAdmin) return true;
        return acceptedTrainerConnection(me, partner) || acceptedTrainerConnection(partner, me);
    }

    /** Accepted TRAINER connection with owner=client, viewer=trainer. */
    private boolean acceptedTrainerConnection(User owner, User viewer) {
        return connectionRepository
                .findByOwnerAndViewerAndType(owner, viewer, ConnectionType.TRAINER)
                .filter(c -> c.getStatus() == ConnectionStatus.ACCEPTED)
                .isPresent();
    }

    private User lowOf(User a, User b) {
        return a.getId() <= b.getId() ? a : b;
    }

    private User highOf(User a, User b) {
        return a.getId() <= b.getId() ? b : a;
    }

    private ChatPartnerDTO toPartner(User user, boolean support) {
        UserInfo info = user.getUserInfo();
        return ChatPartnerDTO.builder()
                .id(user.getId())
                .firstName(info != null ? info.getFirstName() : null)
                .lastName(info != null ? info.getLastName() : null)
                .email(user.getEmail())
                .support(support)
                .build();
    }

    private ChatMessageDTO toDTO(ChatMessage m, User me) {
        User sender = m.getSender();
        boolean fromMe = sender != null && sender.getId().equals(me.getId());
        return ChatMessageDTO.builder()
                .id(m.getId())
                .text(m.getText())
                .fromMe(fromMe)
                .senderName(senderName(sender))
                .createdAt(m.getCreatedAt())
                .build();
    }

    private String senderName(User user) {
        if (user == null) return null;
        UserInfo info = user.getUserInfo();
        String name = info == null ? null
                : java.util.stream.Stream.of(info.getFirstName(), info.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));
        return (name == null || name.isBlank()) ? user.getEmail() : name;
    }
}
