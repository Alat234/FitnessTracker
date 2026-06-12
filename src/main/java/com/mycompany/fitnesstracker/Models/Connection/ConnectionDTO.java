package com.mycompany.fitnesstracker.Models.Connection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Зв'язок між користувачами для відповіді API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionDTO {
    private Long id;
    private ConnectionType type;
    private ConnectionStatus status;
    private ConnectionUserDTO owner;
    private ConnectionUserDTO viewer;
    private PermissionsDTO permissions;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime respondedAt;
}
