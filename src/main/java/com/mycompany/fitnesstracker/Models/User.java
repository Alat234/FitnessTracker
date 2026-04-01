package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.RegistrationType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutProgram;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    private String email;


    private String password;


    @Enumerated(EnumType.STRING)
    private Role role;

    private RegistrationType authProvider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<WorkoutProgram> workoutPrograms = new ArrayList<>();

    @OneToOne(mappedBy = "userIdentity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserInfo userInfo;
     @OneToOne(mappedBy ="gymOwner",fetch = FetchType.LAZY)
    private Gym gym;


    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    @Transient
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }
}
