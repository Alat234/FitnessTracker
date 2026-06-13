package com.mycompany.fitnesstracker.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    @Column(name="gym_name")
    private String name;
    @Column(name="gym_description")
    private String description;
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
    @Column(name="gym_email")
    private String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="gym_owner_id")
    private User gymOwner;

    /* Nullable on purpose: lets Hibernate ddl-auto=update add the column to an
       existing gyms table without failing on pre-existing rows. Set in @PrePersist. */
    @Column(name="gym_created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
