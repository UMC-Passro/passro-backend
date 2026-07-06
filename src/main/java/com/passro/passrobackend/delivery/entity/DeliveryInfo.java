package com.passro.passrobackend.delivery.entity;

import com.passro.passrobackend.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfo extends BaseEntity {
    @GeneratedValue
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Delivery delivery;

    private String picture;
}
