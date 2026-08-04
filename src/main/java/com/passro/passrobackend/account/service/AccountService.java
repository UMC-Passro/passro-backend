package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.accountDTO.AccountReqDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountResDTO;
import com.passro.passrobackend.account.dto.authDTO.AuthReqDTO;
import com.passro.passrobackend.account.dto.authDTO.AuthResDTO;
import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.account.entity.AccountPlace;
import com.passro.passrobackend.account.entity.WayPoint;
import com.passro.passrobackend.account.enums.AccountRole;
import com.passro.passrobackend.account.exception.AccountException;
import com.passro.passrobackend.account.exception.code.AccountErrorCode;
import com.passro.passrobackend.account.repository.AccountPlaceRepository;
import com.passro.passrobackend.account.repository.AccountRepository;
import com.passro.passrobackend.account.repository.UniversityRepository;
import com.passro.passrobackend.account.repository.WayPointRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.file.service.S3Service;
import com.passro.passrobackend.global.jwt.JwtProperties;
import com.passro.passrobackend.global.jwt.JwtProvider;
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.review.dto.ReviewAverageResponseDto;
import com.passro.passrobackend.review.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccountService {

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


    public boolean isNicknameAvailable(String nickname) {
        return !accountRepository.existsByNickname(nickname);
    }

    public AccountResDTO.ShipperMyPage myShipperPage(Long accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountException(AccountErrorCode.NOT_FOUND));

        String picture = null;
        if (account.getPicture() != null) {
            picture = s3Service.getPresignedDownloadUrl(account.getPicture()).toString();
        }

        String nickname = account.getNickname();
        Long deliveryCount = deliveryRepository.countByShipper(account);
        Long point = account.getPoint();

        double rating = 0.0;
        ReviewAverageResponseDto ratingDTO = reviewService.getAverageRating(accountId);
        if(ratingDTO != null)
            rating = ratingDTO.getAverageRating();


        return new AccountResDTO.ShipperMyPage(picture, nickname, deliveryCount, point, rating);
    }

    public AccountResDTO.SenderMyPage mySenderPage(Long accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountException(AccountErrorCode.NOT_FOUND));

        String picture = null;
        if (account.getPicture() != null) {
            picture = s3Service.getPresignedDownloadUrl(account.getPicture()).toString();
        }

        String nickname = account.getNickname();
        Long deliveryCount = deliveryRepository.countBySender(account);
        Long point = account.getPoint();

        return new AccountResDTO.SenderMyPage(picture, nickname, deliveryCount, point);
    }

    @Transactional
    public void editMyInfo(AccountReqDTO.EditMyInfo dto, Long accountId) {

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(EDIT_INFO_COOLDOWN_PREFIX + accountId)))
            throw new AccountException(AccountErrorCode.TOO_FAST);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND));

        if (!account.getNickname().equals(dto.getNickname()) && accountRepository.existsByNickname(dto.getNickname()))
            throw new AccountException(AccountErrorCode.DUPLICATE_NICKNAME);

        if (!account.getPhoneNumber().equals(dto.getPhoneNumber()) && accountRepository.existsByPhoneNumber(dto.getPhoneNumber()))
            throw new AccountException(AccountErrorCode.DUPLICATE_PHONE_NUMBER);

        AccountPlace accountPlace = accountPlaceRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND));

        Place startPlace = placeRepository.findById(dto.getStartPlaceId())
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY));
        Place destinationPlace = placeRepository.findById(dto.getDestinationPlaceId())
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY));

        wayPointRepository.deleteAllByAccountPlace(accountPlace);

        if (dto.getWayPoints() != null) {
            for (int i = 0; i < dto.getWayPoints().size(); i++) {
                Long wayPointPlaceId = dto.getWayPoints().get(i);
                Place wayPointPlace = placeRepository.findById(wayPointPlaceId)
                        .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY));

                wayPointRepository.save(WayPoint.builder()
                        .accountPlace(accountPlace)
                        .place(wayPointPlace)
                        .visitOrder(i)
                        .build());

            }
        }

            accountPlace.changePlace(startPlace, destinationPlace);

            account.changeNickname(dto.getNickname());

            account.changePhoneNumber(dto.getPhoneNumber());

            accountRepository.save(account);

            stringRedisTemplate.opsForValue().set(EDIT_INFO_COOLDOWN_PREFIX + accountId, "true", EDIT_COOLDOWN_TTL);
    }

    public void codeCodeConfirmAndEditPassword(AccountReqDTO.EditPassword dto, Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND));

        String mail = account.getMail();

        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + mail);

        mailSenderService.savedCodeConfirm(code, savedCode);

        if (passwordEncoder.matches(dto.getPassword(), account.getPassword()))
            throw new AccountException(AccountErrorCode.SAME_PASSWORD);

        String editPassword = passwordEncoder.encode(dto.getPassword());

        account.changePassword(editPassword);
        accountRepository.save(account);


        stringRedisTemplate.delete(CODE_PREFIX + mail);
        stringRedisTemplate.opsForValue().set(EDIT_PASSWORD_COOLDOWN_PREFIX + accountId, "true", EDIT_COOLDOWN_TTL);
    }

}
