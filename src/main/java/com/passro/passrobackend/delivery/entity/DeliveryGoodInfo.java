package com.passro.passrobackend.delivery.entity;

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
public class DeliveryGoodInfo extends BaseEntity {
    @GeneratedValue
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Delivery delivery;

    private String name;
    private Long price;
    private String size;
    private String picture;
}
