package com.mycompany.fitnesstracker.Models.GymEntities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycompany.fitnesstracker.Models.Connection.ConnectionUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Повна інформація про зал для відповіді API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String phoneNumber;
    private String email;
    private String imageUrl;
    private Boolean isPublic;
    private ConnectionUserDTO owner;
    private List<GymTrainerDTO> trainers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
