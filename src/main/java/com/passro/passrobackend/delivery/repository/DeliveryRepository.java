package com.passro.passrobackend.delivery.repository;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>
{
    List<Delivery> findAllBySender(Account sender);

    List<Delivery> findAllByStatus(DeliveryState status);

    List<Delivery> findAllByShipper(Account shipper);
}
