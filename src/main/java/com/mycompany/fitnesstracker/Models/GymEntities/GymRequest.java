package com.mycompany.fitnesstracker.Models.GymEntities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тіло запиту на створення / оновлення залу (власник залу).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymRequest {
    private String name;
    private String description;
    private String address;
    private String city;
    private String phoneNumber;
    private String email;
}
