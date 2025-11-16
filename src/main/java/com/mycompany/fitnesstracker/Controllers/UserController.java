package com.mycompany.fitnesstracker.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/test")
@RestController
public class UserController {
    @GetMapping
    public String Hello(){
        return  "Hello World";
    }
}
