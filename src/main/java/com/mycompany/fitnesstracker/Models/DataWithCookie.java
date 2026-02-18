package com.mycompany.fitnesstracker.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseCookie;

@AllArgsConstructor
@Getter
@Setter
public class DataWithCookie<T> {

    private final T data;

    private final ResponseCookie cookie;

}