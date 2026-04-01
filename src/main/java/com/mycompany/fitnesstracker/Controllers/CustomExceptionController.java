package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomExceptionController {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(BaseException e){
        ErrorResponse response=new ErrorResponse(e.getMessage(),e.getStatus().value(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getStatus());

    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthError(BadCredentialsException ex) {
        log.error("Authentication error", ex);
        ErrorResponse response=new ErrorResponse("Invalid email or password", HttpStatus.UNAUTHORIZED.value(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception e){
        log.error("Unexpected error occurred", e);
        ErrorResponse response = new ErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
