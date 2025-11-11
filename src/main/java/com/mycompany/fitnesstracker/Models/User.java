package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    private String email;

    private String password;

    private Role role;
    @OneToOne(mappedBy = "userIdentity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserInfo userInfo;


}
