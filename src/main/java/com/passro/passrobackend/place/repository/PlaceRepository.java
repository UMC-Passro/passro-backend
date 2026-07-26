package com.passro.passrobackend.place.repository;

import com.passro.passrobackend.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {

    List<Place> findByStationNameContaining(String stationName);
}
