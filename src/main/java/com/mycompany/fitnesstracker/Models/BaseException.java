package com.mycompany.fitnesstracker.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class BaseException extends RuntimeException {
    public String message;
    private final HttpStatus status;

}
