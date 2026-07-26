package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.exception.AccountException;
import com.passro.passrobackend.account.exception.code.AccountErrorCode;
import com.passro.passrobackend.place.entity.Place;
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
        List<Place> places = placeRepository.findAllBySubwayStationName(keyword);

        if(!places.isEmpty())
            {
                return places.stream().map(place -> new SubwayApiResDTO.Item(
                        place.getSubwayStationName(),
                        place.getSubwayRouteName()
                )).toList();
            }



        SubwayApiResDTO response = restClient.get()
                .uri(baseUrl + "?serviceKey={apiKey}&subwayStationName={keyword}&_type=json",
                        apiKey, keyword)
                .retrieve()
                .body(SubwayApiResDTO.class);

        if (response == null || response.getResponse() == null
                || response.getResponse().getBody() == null
                || response.getResponse().getBody().getItems() == null)
            throw new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY);

        List<String> subwayRouteList = response
                .getResponse()
                .getBody()
                .getItems()
                .getItem()
                .stream()
                .map(SubwayApiResDTO.Item::getSubwayRouteName)
                .toList();

        subwayRouteList.forEach(subwayName->
                placeRepository.save(
                        Place.builder()
                                .subwayStationName(keyword)
                                .subwayRouteName(subwayName)
                                .build()));

        return response.getResponse().getBody().getItems().getItem();
    }
}
