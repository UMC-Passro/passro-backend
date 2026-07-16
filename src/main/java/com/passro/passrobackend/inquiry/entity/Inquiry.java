package com.passro.passrobackend.inquiry.entity;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.global.entity.BaseEntity;
import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문의가 달린 배송 (ERD: delivery_id)
    @ManyToOne
    private Delivery delivery;

    // 문의자 (ERD: account_id)
    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
