package com.passro.passrobackend.account.repository;

import com.passro.passrobackend.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
