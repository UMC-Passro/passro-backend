package com.passro.passrobackend.inquiry.repository;

import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 특정 배송에 달린 문의 목록 (최신순)
    List<Inquiry> findAllByDeliveryOrderByCreatedAtDesc(Delivery delivery);
}
