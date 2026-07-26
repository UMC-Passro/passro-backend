package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.exception.AccountException;
import com.passro.passrobackend.account.exception.code.AccountErrorCode;
import com.passro.passrobackend.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubwayApiService {

    private final RestClient restClient;
    private final PlaceRepository placeRepository;

    @Value("${public-data.subway.service-key}")
    private String apiKey;

    @Value("${public-data.subway.base-url}")
    private String baseUrl;

    SubwayApiResDTO dto;


    public List<SubwayApiResDTO.Item> searchStation(String keyword){
        SubwayApiResDTO response = restClient.get()
                .uri(baseUrl + "?serviceKey={apiKey}&subwayStationName={keyword}&_type=json",
                        apiKey, keyword)
                .retrieve()
                .body(SubwayApiResDTO.class);

        if (response == null || response.getResponse() == null
                || response.getResponse().getBody() == null
                || response.getResponse().getBody().getItems() == null)
            throw new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY);

        return response.getResponse().getBody().getItems().getItem();
    }
}
