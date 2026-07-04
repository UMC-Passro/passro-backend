package com.passro.passrobackend.delivery.entity;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.global.entity.BaseEntity;
import com.passro.passrobackend.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Place origin;

    @ManyToOne
    private Place dest;

    private Boolean matched;
    private String goodName;
    private Long goodPrice;
    private String goodSize;
    private String picture;
    private String memo;
    private DeliveryState status;
    private Boolean terms;

    @ManyToOne
    private Account matchedAccount;
}
