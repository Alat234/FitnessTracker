package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.RegistrationType;
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

    @Column(name="user_email")
    private String email;

    @Column(name="user_password")
    private String password;

    @Column(name="user_role")
    private Role role;

    @Column(name="user_auth_provider")
    private RegistrationType authProvider;

    @OneToOne(mappedBy = "userIdentity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserInfo userInfo;
     @OneToOne(mappedBy ="gymOwner",fetch = FetchType.LAZY)
    private Gym gym;



}
