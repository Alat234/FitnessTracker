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
    @Column(name = "user_info_id")
    private Long ID;
    @Column(name="user_first_name")
    private String firstName;
    @Column(name="user_last_name")
    private String lastName;
    @Column(name="user_bio")
    private  String BIO;
    @Column(name="user_phone_number")
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name="user_id")
    private User userIdentity;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private  User trainer;

}
