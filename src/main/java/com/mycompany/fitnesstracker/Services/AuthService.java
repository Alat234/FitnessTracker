package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.*;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import com.mycompany.fitnesstracker.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class AuthService {    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public DataWithCookie<AuthenticationResponse> register(RegisterRequest registerRequest) {

        var user= User.builder()
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(Role.ROLE_USER)
                .build();
        var info = UserInfo.builder()
                .firstName(registerRequest.getName())
                .build();

        user.setUserInfo(info);
        info.setUserIdentity(user);
        userRepository.save(user);

        return   generateFullResponse(user);

    }

    public DataWithCookie<AuthenticationResponse> authenticate (AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword())
        );
        var user=userRepository.findUserByEmailIs(authenticationRequest.getEmail())
                .orElseThrow(()-> new BaseException("User not found with email "+ authenticationRequest.getEmail(), HttpStatus.NOT_FOUND));


        return generateFullResponse(user);
    }


    public DataWithCookie<AuthenticationResponse> generateFullResponse(User user) {

        var jwtToken=jwtService.generateToken(user);
        ResponseCookie cookie=ResponseCookie.from("jwt",jwtToken).
                httpOnly(true).
                secure(false).
                path("/").
                maxAge(365 * 24 * 60 * 60).
                build();
        var response=  AuthenticationResponse.builder()
                .email(user.getEmail())
                .name(user.getUserInfo().getFirstName())
                .build();
        return new DataWithCookie<>(response,cookie);


    }


}
