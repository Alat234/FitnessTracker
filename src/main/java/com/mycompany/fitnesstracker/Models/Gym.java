package com.mycompany.fitnesstracker.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name="gyms")
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="gym_id")
    private long id;
    @Column(name="gym_address")
    private String address;
    @Column(name="gym_city")
    private  String city;
    @Column(name="gym_latitude")
    private Double latitude;
    @Column(name="gym_longitude")
    private Double longitude;
    @Column(name="gym_phone_number")
    private  String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="gym_owner_id")
    private User gymOwner;



}
