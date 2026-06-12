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
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private UserConnectionRepository connectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    private ConnectionService service;

    private User owner;
    private User friend;
    private User trainer;

    @BeforeEach
    void setUp() {
        service = new ConnectionService(connectionRepository, userRepository, userService, new ConnectionMapper());
        owner   = user(1L, "owner@test.com", Role.ROLE_USER);
        friend  = user(2L, "friend@test.com", Role.ROLE_USER);
        trainer = user(3L, "trainer@test.com", Role.ROLE_TRAINER);
    }

    /* ── sendInvite ───────────────────────────────────────── */

    @Test
    void sendInvite_friend_success_withFriendDefaultPermissions() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, friend, ConnectionType.FRIEND))
                .thenReturn(Optional.empty());
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        // padded email also proves trim-before-lookup
        ConnectionDTO dto = service.sendInvite(
                new SendInviteRequest("  friend@test.com  ", ConnectionType.FRIEND), "owner@test.com");

        assertThat(dto.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(dto.getType()).isEqualTo(ConnectionType.FRIEND);
        assertThat(dto.getOwner().getEmail()).isEqualTo("owner@test.com");
        assertThat(dto.getViewer().getEmail()).isEqualTo("friend@test.com");
        assertThat(dto.getPermissions().isWorkouts()).isTrue();
        assertThat(dto.getPermissions().isProgressSummary()).isTrue();
        assertThat(dto.getPermissions().isNutrition()).isFalse();
        assertThat(dto.getPermissions().isBodyMetrics()).isFalse();
        verify(userRepository).findUserByEmailIs("friend@test.com");
    }

    @Test
    void sendInvite_trainer_success_whenTargetHasTrainerRole_allPermissionsTrue() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("trainer@test.com")).thenReturn(Optional.of(trainer));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.empty());
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectionDTO dto = service.sendInvite(
                new SendInviteRequest("trainer@test.com", ConnectionType.TRAINER), "owner@test.com");

        assertThat(dto.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(dto.getPermissions().isWorkouts()).isTrue();
        assertThat(dto.getPermissions().isProgressSummary()).isTrue();
        assertThat(dto.getPermissions().isNutrition()).isTrue();
        assertThat(dto.getPermissions().isBodyMetrics()).isTrue();
    }

    @Test
    void sendInvite_trainerType_toNonTrainer_badRequest() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));

        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("friend@test.com", ConnectionType.TRAINER), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Target user is not a trainer");
    }

    @Test
    void sendInvite_self_badRequest() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("owner@test.com")).thenReturn(Optional.of(owner));

        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("owner@test.com", ConnectionType.FRIEND), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Cannot invite yourself");
    }

    @Test
    void sendInvite_unknownEmail_notFound() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("ghost@test.com")).thenReturn(Optional.empty());

        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("ghost@test.com", ConnectionType.FRIEND), "owner@test.com"),
                HttpStatus.NOT_FOUND, "User not found");
    }

    @Test
    void sendInvite_blankEmail_badRequest() {
        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("   ", ConnectionType.FRIEND), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Email is required");
    }

    @Test
    void sendInvite_nullType_badRequest() {
        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("friend@test.com", null), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Connection type is required");
    }

    @Test
    void sendInvite_duplicatePending_conflict() {
        UserConnection existing = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, friend, ConnectionType.FRIEND))
                .thenReturn(Optional.of(existing));

        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("friend@test.com", ConnectionType.FRIEND), "owner@test.com"),
                HttpStatus.CONFLICT, "Invite already exists");
    }

    @Test
    void sendInvite_duplicateAccepted_conflict() {
        UserConnection existing = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.ACCEPTED);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, friend, ConnectionType.FRIEND))
                .thenReturn(Optional.of(existing));

        assertBase(() -> service.sendInvite(
                        new SendInviteRequest("friend@test.com", ConnectionType.FRIEND), "owner@test.com"),
                HttpStatus.CONFLICT, "Invite already exists");
    }

    @Test
    void sendInvite_afterDeclined_reusesRow_resetsToPendingWithDefaultFlags() {
        UserConnection existing = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.DECLINED);
        existing.setRespondedAt(LocalDateTime.now());
        existing.setCanViewNutrition(true); // non-default leftover must be reset

        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, friend, ConnectionType.FRIEND))
                .thenReturn(Optional.of(existing));
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sendInvite(new SendInviteRequest("friend@test.com", ConnectionType.FRIEND), "owner@test.com");

        assertThat(existing.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(existing.getRespondedAt()).isNull();
        assertThat(existing.isCanViewNutrition()).isFalse();
        assertThat(existing.isCanViewWorkouts()).isTrue();
        assertThat(existing.isCanViewProgressSummary()).isTrue();
        verify(connectionRepository).save(existing); // same row, no new entity
    }

    @Test
    void sendInvite_afterRevoked_reusesRow_resetsToPending() {
        UserConnection existing = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.REVOKED);
        existing.setRespondedAt(LocalDateTime.now());

        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(userRepository.findUserByEmailIs("friend@test.com")).thenReturn(Optional.of(friend));
        when(connectionRepository.findByOwnerAndViewerAndType(owner, friend, ConnectionType.FRIEND))
                .thenReturn(Optional.of(existing));
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectionDTO dto = service.sendInvite(
                new SendInviteRequest("friend@test.com", ConnectionType.FRIEND), "owner@test.com");

        assertThat(dto.getStatus()).isEqualTo(ConnectionStatus.PENDING);
        assertThat(dto.getRespondedAt()).isNull();
        verify(connectionRepository).save(existing);
    }

    /* ── accept / decline ─────────────────────────────────── */

    @Test
    void accept_byViewer_setsAcceptedAndRespondedAt() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("friend@test.com")).thenReturn(friend);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectionDTO dto = service.accept(10L, "friend@test.com");

        assertThat(dto.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
        assertThat(conn.getRespondedAt()).isNotNull();
    }

    @Test
    void accept_byNonViewer_forbidden() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));

        assertBase(() -> service.accept(10L, "owner@test.com"),
                HttpStatus.FORBIDDEN, "Only the invited user can respond");
    }

    @Test
    void accept_nonPending_conflict() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.ACCEPTED);
        when(userService.getValidatedUserForAction("friend@test.com")).thenReturn(friend);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));

        assertBase(() -> service.accept(10L, "friend@test.com"),
                HttpStatus.CONFLICT, "Invite already processed");
    }

    @Test
    void decline_byViewer_setsDeclined() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("friend@test.com")).thenReturn(friend);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectionDTO dto = service.decline(10L, "friend@test.com");

        assertThat(dto.getStatus()).isEqualTo(ConnectionStatus.DECLINED);
        assertThat(conn.getRespondedAt()).isNotNull();
    }

    @Test
    void decline_byNonViewer_forbidden() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));

        assertBase(() -> service.decline(10L, "trainer@test.com"),
                HttpStatus.FORBIDDEN, "Only the invited user can respond");
    }

    /* ── revoke ───────────────────────────────────────────── */

    @Test
    void revoke_byOwner_setsRevokedAndRespondedAt() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.ACCEPTED);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));
        when(connectionRepository.save(any(UserConnection.class))).thenAnswer(inv -> inv.getArgument(0));

        service.revoke(10L, "owner@test.com");

        assertThat(conn.getStatus()).isEqualTo(ConnectionStatus.REVOKED);
        assertThat(conn.getRespondedAt()).isNotNull();
    }

    @Test
    void revoke_byNonOwner_forbidden() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.ACCEPTED);
        when(userService.getValidatedUserForAction("friend@test.com")).thenReturn(friend);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));

        assertBase(() -> service.revoke(10L, "friend@test.com"),
                HttpStatus.FORBIDDEN, "Only the owner can revoke access");
    }

    @Test
    void revoke_nonActiveConnection_conflict() {
        UserConnection conn = connection(10L, owner, friend, ConnectionType.FRIEND, ConnectionStatus.DECLINED);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(conn));

        assertBase(() -> service.revoke(10L, "owner@test.com"),
                HttpStatus.CONFLICT, "Connection is not active");
    }

    /* ── helpers ──────────────────────────────────────────── */

    private static User user(long id, String email, Role role) {
        return User.builder().id(id).email(email).role(role).build();
    }

    private static UserConnection connection(long id, User owner, User viewer,
                                             ConnectionType type, ConnectionStatus status) {
        return UserConnection.builder()
                .id(id).owner(owner).viewer(viewer).type(type).status(status)
                .canViewWorkouts(true).canViewProgressSummary(true)
                .build();
    }

    private static void assertBase(ThrowingCallable call, HttpStatus status, String message) {
        assertThatThrownBy(call)
                .isInstanceOf(BaseException.class)
                .satisfies(t -> {
                    BaseException e = (BaseException) t;
                    assertThat(e.getStatus()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }
}
