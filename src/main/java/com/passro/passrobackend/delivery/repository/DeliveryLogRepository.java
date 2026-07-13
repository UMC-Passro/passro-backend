package com.passro.passrobackend.delivery.repository;

import com.passro.passrobackend.delivery.entity.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
}
