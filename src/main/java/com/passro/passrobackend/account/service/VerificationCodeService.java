package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.repository.AccountPlaceRepository;
import com.passro.passrobackend.account.repository.AccountRepository;
import com.passro.passrobackend.account.repository.UniversityRepository;
import com.passro.passrobackend.account.repository.WayPointRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.file.service.S3Service;
import com.passro.passrobackend.global.jwt.JwtProperties;
import com.passro.passrobackend.global.jwt.JwtProvider;
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class VerificationCodeService {

    private final DeliveryRepository deliveryRepository;
    private final ReviewService reviewService;
    private final S3Service s3Service;
    private final MailSenderService mailSenderService;

    private final AccountRepository accountRepository;
    private final UniversityRepository universityRepository;
    private final AccountPlaceRepository accountPlaceRepository;
    private final WayPointRepository wayPointRepository;
    private final PlaceRepository placeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncMailService asyncMailService;
    private final StringRedisTemplate stringRedisTemplate;

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    //인증 코드
    private static final String CODE_PREFIX = "mail:verify:code:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    //인증 자격
    private static final String VERIFIED_PREFIX = "mail:verify:done:";
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    //인증 요청 대기
    private static final String RESEND_COOLDOWN_PREFIX = "mail:verify:cooldown:";
    private static final Duration RESEND_COOLDOWN_TTL = Duration.ofSeconds(60);

    //닉네임 변경 대기
    private static final String EDIT_INFO_COOLDOWN_PREFIX = "edit:info:verify:code";

    //비밀번호 변경 대기
    private static final String EDIT_PASSWORD_COOLDOWN_PREFIX = "edit:password:verify:code";

    //내 정보 변경 시간
    private static final Duration EDIT_COOLDOWN_TTL = Duration.ofMinutes(5);

    private static final String TEMP_PASSWORD_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    private static final String REFRESH_PREFIX = "refresh:token:";

}
