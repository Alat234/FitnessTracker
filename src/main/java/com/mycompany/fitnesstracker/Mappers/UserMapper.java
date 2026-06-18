package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.SelectedGymDTO;
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
            UserInfo source = user.getUserInfo();
            infoDTO = new UserInfoDTO(
                    source.getFirstName(),
                    source.getLastName(),
                    source.getBio(),
                    source.getPhoneNumber(),
                    source.getHeightCm(),
                    source.getWeightKg(),
                    source.getDateOfBirth(),
                    source.getSex(),
                    source.getActivityLevel(),
                    source.getFitnessGoal(),
                    source.getSpecialization(),
                    source.getImageUrl()
            );
        }

        SelectedGymDTO selectedGym = null;
        if (user.getUserInfo() != null && user.getUserInfo().getGym() != null) {
            Gym gym = user.getUserInfo().getGym();
            selectedGym = SelectedGymDTO.builder()
                    .id(gym.getId())
                    .name(gym.getName())
                    .city(gym.getCity())
                    .imageUrl(gym.getImageUrl())
                    .build();
        }

        // Повертаємо основний record DTO
        return new UserDTO(
                user.getEmail(),
                user.getUserInfo() != null ? user.getUserInfo().getFirstName() : null,
                user.getRole(),
                infoDTO,
                selectedGym
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
