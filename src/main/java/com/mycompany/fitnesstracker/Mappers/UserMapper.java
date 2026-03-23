package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Models.UserInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDTO(User user){
        if (user == null){return null;}
        UserInfoDTO infoDTO = null;
        if (user.getUserInfo() != null) {
            infoDTO = new UserInfoDTO(
                    user.getUserInfo().getFirstName(),
                    user.getUserInfo().getLastName(),
                    user.getUserInfo().getBio(),
                    user.getUserInfo().getPhoneNumber()
            );
        }

        // Повертаємо основний record DTO
        return new UserDTO(
                user.getEmail(),
                user.getUserInfo() != null ? user.getUserInfo().getFirstName() : null,
                user.getRole(),
                infoDTO
        );

    }
    public void updateEntityFromDto(UserDTO dto, User entity) {
        if (dto == null || entity == null) return;

        entity.setEmail(dto.email());
        entity.setRole(dto.role());

        if (dto.userInfoDTO() != null) {
            UserInfo info = entity.getUserInfo();

            if (info == null) {
                info = new UserInfo();
                info.setUserIdentity(entity);
                entity.setUserInfo(info);
            }
            info.setFirstName(dto.userInfoDTO().firstName());
            info.setLastName(dto.userInfoDTO().lastName());
            info.setBio(dto.userInfoDTO().BIO());
            info.setPhoneNumber(dto.userInfoDTO().phoneNumber());

        }
    }

}
