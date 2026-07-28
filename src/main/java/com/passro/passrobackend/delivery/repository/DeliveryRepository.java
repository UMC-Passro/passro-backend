package com.passro.passrobackend.delivery.repository;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>
{
    List<Delivery> findAllByStatus(DeliveryState status);

    List<Delivery> findAllByShipper(Account shipper);
    long countByShipper(Account shipper);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Delivery d WHERE d.id = :id")
    Optional<Delivery> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT d
        FROM Delivery d
        LEFT JOIN FETCH d.origin
        LEFT JOIN FETCH d.dest
        WHERE d.sender = :sender
    """)
    List<Delivery> findAllBySender(@Param("sender") Account sender);
    long countBySender(Account sender);
}
