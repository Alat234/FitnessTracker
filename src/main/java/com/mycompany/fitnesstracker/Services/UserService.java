package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.UserMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.BodyMetricsRequest;
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
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    public  UserService( UserRepository userRepository, JwtService jwtService,UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;


    }
    public User getUserByJWt(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        if (email==null){
            throw new BaseException("user not authenticate", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findUserByEmailIs(email)
                        .orElseThrow(()-> new BaseException("user not found",HttpStatus.NOT_FOUND));
    }
    public UserDTO getUserDTOByJWT(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        if (email==null){
            throw new BaseException("user not authenticate", HttpStatus.UNAUTHORIZED);
        }
        User tempUser=userRepository.findUserByEmailIs(email).orElseThrow(()-> new BaseException("user not found",HttpStatus.NOT_FOUND));
        return userMapper.toDTO(tempUser);

    }
    public User getValidatedUserForAction(String email) {
        User user = userRepository.findUserByEmailIs(email)
                .orElseThrow(() -> new BaseException("User not found",HttpStatus.NOT_FOUND));

        return user;
    }

    @Transactional
    public UserDTO updateBodyMetrics(BodyMetricsRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new BaseException("user not authenticate", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findUserByEmailIs(email)
                .orElseThrow(() -> new BaseException("user not found", HttpStatus.NOT_FOUND));

        UserInfo info = user.getUserInfo();
        if (info == null) {
            info = new UserInfo();
            info.setUserIdentity(user);
            user.setUserInfo(info);
        }
        info.setHeightCm(request.getHeightCm());
        info.setWeightKg(request.getWeightKg());
        info.setDateOfBirth(request.getDateOfBirth());
        info.setSex(request.getSex());
        info.setActivityLevel(request.getActivityLevel());
        info.setFitnessGoal(request.getFitnessGoal());

        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    /* ── Personal data ── */
    @Transactional
    public UserDTO updateProfile(UpdateProfileRequest request) {
        User user = currentUser();

        UserInfo info = user.getUserInfo();
        if (info == null) {
            info = new UserInfo();
            info.setUserIdentity(user);
            user.setUserInfo(info);
        }
        info.setFirstName(trimToNull(request.getFirstName()));
        info.setLastName(trimToNull(request.getLastName()));
        info.setPhoneNumber(trimToNull(request.getPhoneNumber()));
        info.setBio(trimToNull(request.getBio()));

        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    /* ── Password change ── */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();

        if (user.getAuthProvider() != RegistrationType.LOCAL
                || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BaseException("Password change is only available for password-based accounts.",
                    HttpStatus.BAD_REQUEST);
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BaseException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /* ── Trainer public profile (ROLE_TRAINER only) ── */
    @Transactional
    public UserDTO updateTrainerProfile(UpdateTrainerProfileRequest request) {
        User user = currentUser();
        if (user.getRole() != Role.ROLE_TRAINER) {
            throw new BaseException("Trainer role required", HttpStatus.FORBIDDEN);
        }

        UserInfo info = user.getUserInfo();
        if (info == null) {
            info = new UserInfo();
            info.setUserIdentity(user);
            user.setUserInfo(info);
        }
        info.setSpecialization(trimToNull(request.getSpecialization()));
        info.setImageUrl(trimToNull(request.getImageUrl()));

        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new BaseException("user not authenticate", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findUserByEmailIs(email)
                .orElseThrow(() -> new BaseException("user not found", HttpStatus.NOT_FOUND));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
