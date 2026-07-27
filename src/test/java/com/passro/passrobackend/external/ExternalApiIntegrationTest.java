package com.passro.passrobackend.external;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.service.SubwayApiService;
import com.passro.passrobackend.file.exception.FileException;
import com.passro.passrobackend.file.service.S3Service;
import com.passro.passrobackend.support.IntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class ExternalApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private S3Service s3Service;

    @MockitoBean
    private SubwayApiService subwayApiService;

    @Test
    void uploadAndDownloadPresignedUrlsAreReturned() throws Exception {
        mockMvc.perform(get("/file/{fileName}/upload", "profile.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", containsString("profile.png")))
                .andExpect(jsonPath("$.result", containsString("X-Amz-Signature")));

        mockMvc.perform(get("/file/{fileName}/download", "profile.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", containsString("profile.png")))
                .andExpect(jsonPath("$.result", containsString("X-Amz-Signature")));
    }

    @Test
    void invalidPresignedUrlArgumentsAreRejected() {
        assertThatThrownBy(() -> s3Service.getPresignedUploadUrl("", Duration.ofMinutes(10), null))
                .isInstanceOf(FileException.class);
        assertThatThrownBy(() -> s3Service.getPresignedDownloadUrl("profile.png", Duration.ofDays(8)))
                .isInstanceOf(FileException.class);
    }

    @Test
    void subwaySearchReturnsExternalBoundaryResult() throws Exception {
        SubwayApiResDTO.Item station = mock(SubwayApiResDTO.Item.class);
        given(station.getSubwayStationName()).willReturn("Gangnam");
        given(station.getSubwayRouteName()).willReturn("Line 2");
        given(subwayApiService.searchStation("2")).willReturn(List.of(station));

        mockMvc.perform(get("/subway/search").param("keyword", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].subwayStationName").value("Gangnam"));
    }

    @Test
    void subwaySearchRejectsInvalidKeyword() throws Exception {
        mockMvc.perform(get("/subway/search").param("keyword", "Gangnam!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }
}
