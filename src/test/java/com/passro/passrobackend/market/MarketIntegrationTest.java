package com.passro.passrobackend.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.market.entity.Market;
import com.passro.passrobackend.market.repository.MarketRepository;
import com.passro.passrobackend.point.entity.PointLog;
import com.passro.passrobackend.point.enums.PointIncrementReason;
import com.passro.passrobackend.point.repository.PointLogRepository;
import com.passro.passrobackend.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MarketIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Test
    void userCanPurchaseMarketItemWithPoints() throws Exception {
        Account account = createAccount("market-buyer");
        account.setPoint(5000L);
        accountRepository.saveAndFlush(account);
        Market market = saveMarket("커피 쿠폰", 3000L);

        mockMvc.perform(post("/market/{marketId}/purchase", market.getId())
                        .header("Authorization", bearer(accessToken(account))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MARKET200_1"))
                .andExpect(jsonPath("$.result.item.id").value(market.getId()))
                .andExpect(jsonPath("$.result.item.name").value("커피 쿠폰"))
                .andExpect(jsonPath("$.result.beforePoint").value(5000))
                .andExpect(jsonPath("$.result.usedPoint").value(3000))
                .andExpect(jsonPath("$.result.remainingPoint").value(2000));

        entityManager.clear();
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.currentPoint()).isEqualTo(2000L);

        List<PointLog> logs = pointLogRepository.findAllByAccountOrderByCreatedAtDesc(updatedAccount);
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getIncrementReason()).isEqualTo(PointIncrementReason.MARKET_PURCHASE);
        assertThat(logs.getFirst().getMarket().getId()).isEqualTo(market.getId());
        assertThat(logs.getFirst().getDelivery()).isNull();
        assertThat(logs.getFirst().getDeltaPoint()).isEqualTo(-3000L);
        assertThat(logs.getFirst().getBeforePoint()).isEqualTo(5000L);
        assertThat(logs.getFirst().getAfterPoint()).isEqualTo(2000L);
    }

    @Test
    void purchaseFailsWhenPointsAreInsufficient() throws Exception {
        Account account = createAccount("poor-buyer");
        account.setPoint(1000L);
        accountRepository.saveAndFlush(account);
        Market market = saveMarket("영화 쿠폰", 5000L);

        mockMvc.perform(post("/market/{marketId}/purchase", market.getId())
                        .header("Authorization", bearer(accessToken(account))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POINT409_1"));

        entityManager.clear();
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.currentPoint()).isEqualTo(1000L);
        assertThat(pointLogRepository.findAllByAccountOrderByCreatedAtDesc(updatedAccount)).isEmpty();
    }

    @Test
    void purchaseFailsWhenMarketItemDoesNotExist() throws Exception {
        Account account = createAccount("missing-item-buyer");

        mockMvc.perform(post("/market/{marketId}/purchase", Long.MAX_VALUE)
                        .header("Authorization", bearer(accessToken(account))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MARKET404_1"));
    }

    @Test
    void userCanReadMarketItems() throws Exception {
        Account account = createAccount("market-reader");
        Market market = saveMarket("편의점 쿠폰", 2000L);

        mockMvc.perform(get("/market")
                        .header("Authorization", bearer(accessToken(account))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[?(@.id == %d)].name".formatted(market.getId()))
                        .value(hasItem("편의점 쿠폰")));
    }

    private Market saveMarket(String name, long price) {
        return marketRepository.saveAndFlush(Market.builder()
                .name(name)
                .price(price)
                .build());
    }
}
