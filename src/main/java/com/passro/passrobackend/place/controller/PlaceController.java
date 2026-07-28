package com.passro.passrobackend.place.controller;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.place.service.PlaceService;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/subway")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/search")
    public APIResponse<List<SubwayApiResDTO.Item>> search(
            @RequestParam
            @Pattern(regexp = "^[가-힣0-9]+$", message = "검색어는 한글과 숫자만 입력 가능합니다.")
            String keyword) {
        BaseSuccessCode code = AccountSuccessCode.OK;
        List<SubwayApiResDTO.Item> result = placeService.searchByKeyword(keyword).stream()
                .map(place -> new SubwayApiResDTO.Item(
                        place.getId(),
                        place.getSubwayStationName(),
                        place.getSubwayRouteName()))
                .toList();
        return APIResponse.onSuccess(code, result);
    }
}
