package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.*;
import com.mycompany.fitnesstracker.Services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private  final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registration(@RequestBody RegisterRequest registerRequest){

        DataWithCookie<UserDTO> result = authService.register(registerRequest);
        return ResponseEntity.ok()
                .header("Set-Cookie",result.getCookie().toString())
                .body(result.getData());

    }
    @PostMapping("/authentication")
    public ResponseEntity<UserDTO> authentication(@RequestBody AuthenticationRequest authenticationRecord){

        DataWithCookie<UserDTO> result = authService.authenticate(authenticationRecord);
        return ResponseEntity.ok()
                .header("Set-Cookie", result.getCookie().toString())
                .body(result.getData());

    }


}
