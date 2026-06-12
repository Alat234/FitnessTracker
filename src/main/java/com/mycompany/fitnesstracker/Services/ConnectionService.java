package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ConnectionMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Connection.ConnectionDTO;
import com.mycompany.fitnesstracker.Models.Connection.SendInviteRequest;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {

    private final UserConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ConnectionMapper connectionMapper;

    /* ═══════════════════════════════════════════════
       INVITES
    ═══════════════════════════════════════════════ */

    /**
     * Власник (owner) надсилає запрошення користувачу за email.
     * TRAINER-запрошення можна надіслати лише користувачу з роллю ROLE_TRAINER.
     */
    @Transactional
    public ConnectionDTO sendInvite(SendInviteRequest request, String ownerEmail) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BaseException("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getType() == null) {
            throw new BaseException("Connection type is required", HttpStatus.BAD_REQUEST);
        }

        User owner = userService.getValidatedUserForAction(ownerEmail);

        String targetEmail = request.getEmail().trim();
        User viewer = userRepository.findUserByEmailIs(targetEmail)
                .orElseThrow(() -> new BaseException("User not found", HttpStatus.NOT_FOUND));

        if (viewer.getId().equals(owner.getId())) {
            throw new BaseException("Cannot invite yourself", HttpStatus.BAD_REQUEST);
        }
        if (request.getType() == ConnectionType.TRAINER && viewer.getRole() != Role.ROLE_TRAINER) {
            throw new BaseException("Target user is not a trainer", HttpStatus.BAD_REQUEST);
        }

        Optional<UserConnection> existing =
                connectionRepository.findByOwnerAndViewerAndType(owner, viewer, request.getType());

        UserConnection connection;
        if (existing.isPresent()) {
            connection = existing.get();
            if (connection.getStatus() == ConnectionStatus.PENDING
                    || connection.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new BaseException("Invite already exists", HttpStatus.CONFLICT);
            }
            /* DECLINED / REVOKED — повторне запрошення: той самий рядок назад у PENDING */
            connection.setStatus(ConnectionStatus.PENDING);
            connection.setRespondedAt(null);
        } else {
            connection = UserConnection.builder()
                    .owner(owner)
                    .viewer(viewer)
                    .type(request.getType())
                    .status(ConnectionStatus.PENDING)
                    .build();
        }
        applyDefaultPermissions(connection, request.getType());

        return connectionMapper.toDTO(connectionRepository.save(connection));
    }

    /* Вихідні запрошення поточного користувача (він — owner) */
    @Transactional
    public List<ConnectionDTO> getOutgoing(String ownerEmail) {
        User owner = userService.getValidatedUserForAction(ownerEmail);
        return connectionRepository.findAllByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(connectionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* Вхідні запрошення поточного користувача (він — viewer): PENDING + ACCEPTED */
    @Transactional
    public List<ConnectionDTO> getIncoming(String viewerEmail) {
        User viewer = userService.getValidatedUserForAction(viewerEmail);
        return connectionRepository
                .findAllByViewerAndStatusInOrderByCreatedAtDesc(
                        viewer, List.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED))
                .stream()
                .map(connectionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /* ═══════════════════════════════════════════════
       RESPONSES
    ═══════════════════════════════════════════════ */

    /** Viewer приймає запрошення (лише зі статусу PENDING). */
    @Transactional
    public ConnectionDTO accept(Long connectionId, String viewerEmail) {
        return respond(connectionId, viewerEmail, ConnectionStatus.ACCEPTED);
    }

    /** Viewer відхиляє запрошення (лише зі статусу PENDING). */
    @Transactional
    public ConnectionDTO decline(Long connectionId, String viewerEmail) {
        return respond(connectionId, viewerEmail, ConnectionStatus.DECLINED);
    }

    /** Owner відкликає доступ (з PENDING або ACCEPTED). Рядок зберігається для повторного запрошення. */
    @Transactional
    public void revoke(Long connectionId, String ownerEmail) {
        User owner = userService.getValidatedUserForAction(ownerEmail);
        UserConnection connection = getConnectionOrThrow(connectionId);

        if (!connection.getOwner().getId().equals(owner.getId())) {
            throw new BaseException("Only the owner can revoke access", HttpStatus.FORBIDDEN);
        }
        if (connection.getStatus() != ConnectionStatus.PENDING
                && connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new BaseException("Connection is not active", HttpStatus.CONFLICT);
        }

        connection.setStatus(ConnectionStatus.REVOKED);
        connection.setRespondedAt(LocalDateTime.now());
        connectionRepository.save(connection);
    }

    /* ═══════════════════════════════════════════════
       HELPERS
    ═══════════════════════════════════════════════ */

    private ConnectionDTO respond(Long connectionId, String viewerEmail, ConnectionStatus newStatus) {
        User viewer = userService.getValidatedUserForAction(viewerEmail);
        UserConnection connection = getConnectionOrThrow(connectionId);

        if (!connection.getViewer().getId().equals(viewer.getId())) {
            throw new BaseException("Only the invited user can respond", HttpStatus.FORBIDDEN);
        }
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BaseException("Invite already processed", HttpStatus.CONFLICT);
        }

        connection.setStatus(newStatus);
        connection.setRespondedAt(LocalDateTime.now());
        return connectionMapper.toDTO(connectionRepository.save(connection));
    }

    private UserConnection getConnectionOrThrow(Long id) {
        return connectionRepository.findById(id)
                .orElseThrow(() -> new BaseException("Connection not found", HttpStatus.NOT_FOUND));
    }

    /* Дефолтні права: тренер бачить усе, друг — лише тренування та підсумок прогресу */
    private void applyDefaultPermissions(UserConnection connection, ConnectionType type) {
        boolean isTrainer = (type == ConnectionType.TRAINER);
        connection.setCanViewWorkouts(true);
        connection.setCanViewProgressSummary(true);
        connection.setCanViewNutrition(isTrainer);
        connection.setCanViewBodyMetrics(isTrainer);
    }
}
