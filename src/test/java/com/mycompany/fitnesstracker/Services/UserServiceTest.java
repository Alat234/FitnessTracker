package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.UserMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.ChangePasswordRequest;
import com.mycompany.fitnesstracker.Models.UpdateProfileRequest;
import com.mycompany.fitnesstracker.Models.UpdateTrainerProfileRequest;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Models.Enums.RegistrationType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import com.mycompany.fitnesstracker.config.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private static final String EMAIL = "user@test.com";

    private UserRepository userRepository;
    private JwtService jwtService;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserService(userRepository, jwtService, userMapper, passwordEncoder);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, "x"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User localUser(String passwordHash) {
        User user = User.builder()
                .id(1L)
                .email(EMAIL)
                .role(Role.ROLE_USER)
                .password(passwordHash)
                .authProvider(RegistrationType.LOCAL)
                .build();
        UserInfo info = UserInfo.builder().userIdentity(user).build();
        user.setUserInfo(info);
        return user;
    }

    private ChangePasswordRequest pwRequest(String current, String next) {
        ChangePasswordRequest r = new ChangePasswordRequest();
        r.setCurrentPassword(current);
        r.setNewPassword(next);
        return r;
    }

    @Test
    void updateProfile_setsPersonalFields_andSaves() {
        User user = localUser("hash");
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(
                UserDTO.builder().email(EMAIL).role(Role.ROLE_USER).build());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("  John  ");
        req.setLastName("Doe");
        req.setPhoneNumber("+123456");
        req.setBio("Lifting enthusiast");

        service.updateProfile(req);

        assertEquals("John", user.getUserInfo().getFirstName()); // trimmed
        assertEquals("Doe", user.getUserInfo().getLastName());
        assertEquals("+123456", user.getUserInfo().getPhoneNumber());
        assertEquals("Lifting enthusiast", user.getUserInfo().getBio());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_doesNotChangeEmailRoleOrPassword() {
        User user = localUser("hash");
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(
                UserDTO.builder().email(EMAIL).role(Role.ROLE_USER).build());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("New");

        service.updateProfile(req);

        assertEquals(EMAIL, user.getEmail());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertEquals("hash", user.getPassword());
    }

    @Test
    void updateProfile_blankFieldBecomesNull() {
        User user = localUser("hash");
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(UserDTO.builder().email(EMAIL).build());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("   ");
        req.setBio("only bio");

        service.updateProfile(req);

        assertNull(user.getUserInfo().getFirstName());
        assertEquals("only bio", user.getUserInfo().getBio());
    }

    @Test
    void changePassword_rejectsNonLocalAccount() {
        User user = localUser("hash");
        user.setAuthProvider(RegistrationType.GOOGLE);
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BaseException.class,
                () -> service.changePassword(pwRequest("whatever", "newpassword")));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void changePassword_rejectsPasswordlessAccount() {
        User user = localUser(null);
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BaseException.class,
                () -> service.changePassword(pwRequest("whatever", "newpassword")));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void changePassword_wrongCurrentPassword_fails() {
        User user = localUser("hash");
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(BaseException.class,
                () -> service.changePassword(pwRequest("wrong", "newpassword")));
        verify(userRepository, never()).save(any());
        assertEquals("hash", user.getPassword());
    }

    @Test
    void changePassword_success_encodesAndSaves() {
        User user = localUser("hash");
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("right", "hash")).thenReturn(true);
        when(passwordEncoder.encode("newpassword")).thenReturn("newHash");

        service.changePassword(pwRequest("right", "newpassword"));

        assertEquals("newHash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void userDTO_doesNotExposePassword() {
        boolean hasPassword = Arrays.stream(UserDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch(name -> name.toLowerCase().contains("password"));
        assertTrue(!hasPassword, "UserDTO must not expose a password field");
    }

    /* ── Trainer public profile ── */

    private User trainerUser() {
        User user = User.builder()
                .id(1L).email(EMAIL).role(Role.ROLE_TRAINER)
                .password("hash").authProvider(RegistrationType.LOCAL).build();
        user.setUserInfo(UserInfo.builder().userIdentity(user).build());
        return user;
    }

    private UpdateTrainerProfileRequest trainerReq(String spec, String img) {
        UpdateTrainerProfileRequest r = new UpdateTrainerProfileRequest();
        r.setSpecialization(spec);
        r.setImageUrl(img);
        return r;
    }

    @Test
    void updateTrainerProfile_trainer_setsFields_andSaves() {
        User user = trainerUser();
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(UserDTO.builder().email(EMAIL).build());

        service.updateTrainerProfile(trainerReq("  Strength  ", "https://img/t.jpg"));

        assertEquals("Strength", user.getUserInfo().getSpecialization()); // trimmed
        assertEquals("https://img/t.jpg", user.getUserInfo().getImageUrl());
        verify(userRepository).save(user);
    }

    @Test
    void updateTrainerProfile_nonTrainer_forbidden_noSave() {
        User user = localUser("hash"); // ROLE_USER
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BaseException.class,
                () -> service.updateTrainerProfile(trainerReq("Strength", "https://img/t.jpg")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateTrainerProfile_doesNotChangeEmailRoleOrPassword() {
        User user = trainerUser();
        when(userRepository.findUserByEmailIs(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(UserDTO.builder().email(EMAIL).build());

        service.updateTrainerProfile(trainerReq("Yoga", null));

        assertEquals(EMAIL, user.getEmail());
        assertEquals(Role.ROLE_TRAINER, user.getRole());
        assertEquals("hash", user.getPassword());
    }
}
