package com.passro.passrobackend.account.entity;

import com.passro.passrobackend.global.entity.BaseEntity;
import com.passro.passrobackend.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String nickname;

    @ManyToOne
    private Place place_id;

    private String name;
    private String phone;
    private LocalDate birth;
    private Boolean certified;
    private Long point;
    private String picture;
}
