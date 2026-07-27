package com.passro.passrobackend.subway;

import static org.assertj.core.api.Assertions.assertThat;

import com.passro.passrobackend.subway.graph.SubwayEdge;
import com.passro.passrobackend.subway.graph.SubwayNode;
import com.passro.passrobackend.subway.service.SubwayService;
import com.passro.passrobackend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SubwayServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private SubwayService subwayService;

    @Test
    void sameStationOnDifferentRoutesUsesDistinctNodesAndTransferEdges() {
        SubwayNode bundangJeongja = requiredNode("수인분당", "정자");
        SubwayNode shinbundangJeongja = requiredNode("신분당", "정자");

        assertThat(bundangJeongja.getId()).isNotEqualTo(shinbundangJeongja.getId());
        assertThat(edgeBetween(bundangJeongja, shinbundangJeongja).isCrossroute()).isTrue();
        assertThat(edgeBetween(shinbundangJeongja, bundangJeongja).isCrossroute()).isTrue();
    }

    @Test
    void routeCanContinueThroughJeongjaTransfer() {
        SubwayNode bundangMigeum = requiredNode("수인분당", "미금(분당서울대병원)");
        SubwayNode bundangJeongja = requiredNode("수인분당", "정자");
        SubwayNode shinbundangJeongja = requiredNode("신분당", "정자");
        SubwayNode shinbundangPangyo = requiredNode("신분당", "판교(판교테크노밸리)");

        assertThat(edgeBetween(bundangMigeum, bundangJeongja).isCrossroute()).isFalse();
        assertThat(edgeBetween(bundangJeongja, shinbundangJeongja).isCrossroute()).isTrue();
        assertThat(edgeBetween(shinbundangJeongja, shinbundangPangyo).isCrossroute()).isFalse();
    }

    @Test
    void nonCapitalRoutesUseRegionPrefix() {
        SubwayNode capitalCityHall = requiredNode("1호선", "시청");
        SubwayNode daejeonCityHall = requiredNode("대전 1호선", "시청");

        assertThat(capitalCityHall.getId()).isNotEqualTo(daejeonCityHall.getId());
        assertThat(subwayService.findNode("대구 1호선", "교대")).isPresent();
        assertThat(subwayService.findNode("부산 1호선", "교대")).isPresent();
    }

    private SubwayNode requiredNode(String routeName, String stationName) {
        return subwayService.findNode(routeName, stationName).orElseThrow();
    }

    private SubwayEdge edgeBetween(SubwayNode source, SubwayNode target) {
        return source.getEdges().stream()
                .filter(edge -> edge.getTarget().getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
    }
}
