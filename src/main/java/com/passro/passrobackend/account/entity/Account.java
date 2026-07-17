package com.passro.passrobackend.account.entity;

import com.passro.passrobackend.account.enums.AccountRole;
import com.passro.passrobackend.global.entity.BaseEntity;
import com.passro.passrobackend.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String nickname;

    @ManyToOne
    private Place place_id;

    private String name;
    private String phone;
    private LocalDate birth;
    private Boolean certified;
    private Long point;
    private String picture;

    @Enumerated(EnumType.STRING)
    private AccountRole role;
}
