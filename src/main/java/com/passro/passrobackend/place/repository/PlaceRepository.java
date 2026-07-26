package com.passro.passrobackend.place.repository;

import com.passro.passrobackend.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findAllBySubwayStationName(String stationName);
}
