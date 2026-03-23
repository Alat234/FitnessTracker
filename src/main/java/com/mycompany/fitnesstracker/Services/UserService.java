package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.UserMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import com.mycompany.fitnesstracker.config.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Security;
@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    public  UserService( UserRepository userRepository, JwtService jwtService,UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;


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
}
