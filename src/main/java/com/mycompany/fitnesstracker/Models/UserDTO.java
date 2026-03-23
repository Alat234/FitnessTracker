package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.Role;
import lombok.Builder;

@Builder
public record UserDTO(String email,String firstName, Role role, UserInfoDTO userInfoDTO) {

}
