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
    public ResponseEntity<AuthenticationResponse> registration(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));

    }
    @PostMapping("/authentication")
    public ResponseEntity<AuthenticationResponse> authentication(@RequestBody AuthenticationRequest authenticationRecord){
        return ResponseEntity.ok(authService.authenticate(authenticationRecord));
    }


}
