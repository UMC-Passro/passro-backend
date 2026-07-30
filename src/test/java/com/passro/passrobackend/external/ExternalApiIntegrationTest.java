package com.passro.passrobackend.external;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.file.exception.FileException;
import com.passro.passrobackend.file.service.S3Service;
import com.passro.passrobackend.support.IntegrationTestSupport;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExternalApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private S3Service s3Service;

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
    void subwaySearchFindsRouteNameContainingKeyword() throws Exception {
        mockMvc.perform(get("/subway/stations").param("keyword", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUBWAY200_2"))
                .andExpect(jsonPath("$.result[0].region").doesNotExist())
                .andExpect(jsonPath("$.result[*].routeName", hasItem("2호선")));
    }

    @Test
    void subwaySearchFindsStationNameContainingKeyword() throws Exception {
        mockMvc.perform(get("/subway/stations").param("keyword", "강남"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[*].stationName", hasItem("강남")));
    }

    @Test
    void subwaySearchRejectsInvalidKeyword() throws Exception {
        mockMvc.perform(get("/subway/stations").param("keyword", "Gangnam!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
    }
}
