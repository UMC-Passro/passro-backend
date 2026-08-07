package com.passro.passrobackend.market.service;

import com.passro.passrobackend.market.dto.MarketItemResponseDto;
import com.passro.passrobackend.market.dto.MarketPurchaseResponseDto;
import com.passro.passrobackend.market.entity.Market;
import com.passro.passrobackend.market.exception.MarketException;
import com.passro.passrobackend.market.exception.code.MarketErrorCode;
import com.passro.passrobackend.market.repository.MarketRepository;
import com.passro.passrobackend.point.service.PointService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketService {

    private final MarketRepository marketRepository;
    private final PointService pointService;

    public List<MarketItemResponseDto> getItems() {
        return marketRepository.findAllByOrderByIdAsc().stream()
                .map(MarketItemResponseDto::from)
                .toList();
    }

    @Transactional
    public MarketPurchaseResponseDto purchase(Long accountId, Long marketId) {
        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        long remainingPoint = pointService.payForMarket(accountId, market);
        long beforePoint = Math.addExact(remainingPoint, market.getPrice());
        return MarketPurchaseResponseDto.of(market, beforePoint, remainingPoint);
    }
}
