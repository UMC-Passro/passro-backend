package com.passro.passrobackend.review.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.review.code.ReviewSuccessCode;
import com.passro.passrobackend.review.dto.ReviewAverageResponseDto;
import com.passro.passrobackend.review.dto.ReviewCreateRequestDto;
import com.passro.passrobackend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public APIResponse<String> createReview(
            @AuthenticationPrincipal Account account,
            @RequestBody ReviewCreateRequestDto request
    ) {
        reviewService.createReview(account, request);
        return APIResponse.onSuccess(ReviewSuccessCode.REVIEW_CREATED, "리뷰 작성이 완료되었습니다.");
    }

    @GetMapping("/average/{userId}")
    public APIResponse<ReviewAverageResponseDto> getAverageRating(@PathVariable Long userId) {
        return APIResponse.onSuccess(
                ReviewSuccessCode.REVIEW_AVERAGE_RATING_FOUND,
                reviewService.getAverageRating(userId)
        );
    }
}
