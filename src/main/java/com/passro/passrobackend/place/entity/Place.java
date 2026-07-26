package com.passro.passrobackend.place.entity;

import com.passro.passrobackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Place extends BaseEntity {

    @Id
    @Column(name = "subway_station_id", length = 20)
    private String id;

    @Column(nullable = false, length = 50)
    private String stationName;

    @Column(nullable = false, length = 30)
    private String routeName;
}
