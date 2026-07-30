package com.passro.passrobackend.place.service;

import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.place.repository.PlaceRepository;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

    private static final String SUBWAY_DATA_PATH = "subway_station_data.csv";
    private static final Charset SUBWAY_DATA_CHARSET = Charset.forName("MS949");
    private static final String CAPITAL_REGION = "수도권";
    private static final String REGION_COLUMN = "권역명";
    private static final String ROUTE_COLUMN = "노선명";
    private static final String STATION_COLUMN = "역명";

    private final PlaceRepository placeRepository;

    @PostConstruct
    public void initialize() {
        if (placeRepository.count() > 0) {
            return;
        }

        List<Place> places = readSubwayPlaces();
        placeRepository.saveAll(places);
        log.info("지하철역 초기 데이터 {}건을 저장했습니다.", places.size());
    }

    public List<Place> searchByKeyword(String keyword) {
        return placeRepository
                .findAllBySubwayRouteNameContainingIgnoreCaseOrSubwayStationNameContainingIgnoreCase(
                        keyword, keyword);
    }

    private List<Place> readSubwayPlaces() {
        ClassPathResource resource = new ClassPathResource(SUBWAY_DATA_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), SUBWAY_DATA_CHARSET))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("지하철역 CSV 파일이 비어 있습니다: " + SUBWAY_DATA_PATH);
            }

            List<String> headers = parseCsvLine(removeBom(headerLine));
            int regionIndex = findColumnIndex(headers, REGION_COLUMN);
            int routeIndex = findColumnIndex(headers, ROUTE_COLUMN);
            int stationIndex = findColumnIndex(headers, STATION_COLUMN);
            int requiredColumnCount = Math.max(regionIndex, Math.max(routeIndex, stationIndex)) + 1;

            Map<String, Place> uniquePlaces = new LinkedHashMap<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                if (columns.size() < requiredColumnCount) {
                    throw new IllegalStateException("지하철역 CSV " + lineNumber + "행의 컬럼 수가 부족합니다.");
                }

                String regionName = columns.get(regionIndex).trim();
                String originalRouteName = columns.get(routeIndex).trim();
                String stationName = columns.get(stationIndex).trim();
                if (regionName.isEmpty() || originalRouteName.isEmpty() || stationName.isEmpty()) {
                    throw new IllegalStateException(
                            "지하철역 CSV " + lineNumber + "행의 권역명, 노선명 또는 역명이 비어 있습니다.");
                }

                String routeName = normalizeRouteName(regionName, originalRouteName);
                String key = routeName + '\u0000' + stationName;
                uniquePlaces.putIfAbsent(key, Place.builder()
                        .subwayRouteName(routeName)
                        .subwayStationName(stationName)
                        .build());
            }

            return new ArrayList<>(uniquePlaces.values());
        } catch (IOException exception) {
            throw new IllegalStateException("지하철역 CSV 파일을 읽을 수 없습니다: " + SUBWAY_DATA_PATH, exception);
        }
    }

    private String normalizeRouteName(String regionName, String routeName) {
        if (CAPITAL_REGION.equals(regionName)) {
            return routeName;
        }
        return regionName + " " + routeName;
    }

    private int findColumnIndex(List<String> headers, String columnName) {
        for (int index = 0; index < headers.size(); index++) {
            if (columnName.equals(headers.get(index).trim())) {
                return index;
            }
        }
        throw new IllegalStateException("지하철역 CSV에 '" + columnName + "' 컬럼이 없습니다.");
    }

    private String removeBom(String line) {
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        if (quoted) {
            throw new IllegalStateException("지하철역 CSV에 닫히지 않은 따옴표가 있습니다.");
        }

        values.add(value.toString());
        return values;
    }
}
