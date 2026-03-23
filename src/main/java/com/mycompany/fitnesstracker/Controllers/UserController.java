package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController

public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserByJWT(){
        return ResponseEntity.ok(userService.getUserDTOByJWT());
    }

}
