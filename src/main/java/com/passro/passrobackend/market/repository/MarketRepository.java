package com.passro.passrobackend.market.repository;

import com.passro.passrobackend.market.entity.Market;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {

    List<Market> findAllByOrderByIdAsc();
}
