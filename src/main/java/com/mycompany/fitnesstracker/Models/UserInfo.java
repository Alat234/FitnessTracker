package com.mycompany.fitnesstracker.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name= "user_info")
public class UserInfo {

    @Id
    @Column(name = "user_id")
    private Long ID;

    private String firstName;

    private String lastName;

    private  String BIO;

    private String phoneNumber;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name="user_id")
    private User userIdentity;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private  User trainer;

}
