package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.Connection.ConnectionDTO;
import com.mycompany.fitnesstracker.Models.Connection.ConnectionUserDTO;
import com.mycompany.fitnesstracker.Models.Connection.PermissionsDTO;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class ConnectionMapper {

    /* ── UserConnection → ConnectionDTO ────────────────────── */
    public ConnectionDTO toDTO(UserConnection entity) {
        if (entity == null) return null;
        return ConnectionDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .status(entity.getStatus())
                .owner(toUserDTO(entity.getOwner()))
                .viewer(toUserDTO(entity.getViewer()))
                .permissions(PermissionsDTO.builder()
                        .workouts(entity.isCanViewWorkouts())
                        .nutrition(entity.isCanViewNutrition())
                        .bodyMetrics(entity.isCanViewBodyMetrics())
                        .progressSummary(entity.isCanViewProgressSummary())
                        .build())
                .createdAt(entity.getCreatedAt())
                .respondedAt(entity.getRespondedAt())
                .build();
    }

    /* ── User → ConnectionUserDTO (лише публічні поля) ─────── */
    public ConnectionUserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserInfo info = user.getUserInfo();
        return ConnectionUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(info != null ? info.getFirstName() : null)
                .lastName(info != null ? info.getLastName() : null)
                .build();
    }
}
