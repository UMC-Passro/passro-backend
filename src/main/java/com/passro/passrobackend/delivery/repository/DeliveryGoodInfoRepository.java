package com.passro.passrobackend.delivery.repository;

import com.passro.passrobackend.delivery.entity.DeliveryGoodInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryGoodInfoRepository extends JpaRepository<DeliveryGoodInfo, Long> {
}
