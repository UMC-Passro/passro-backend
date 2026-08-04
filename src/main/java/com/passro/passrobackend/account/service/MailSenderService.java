package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.authDTO.AuthReqDTO;
import com.passro.passrobackend.account.entity.Account;
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
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class MailSenderService {

    private final DeliveryRepository deliveryRepository;
    private final ReviewService reviewService;
    private final S3Service s3Service;

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


    public void sendMailMessageSignUpOrShipperSelect(AuthReqDTO.SendMail dto) {
        String mail = dto.getMail();

        if(dto.isStudent())
            validateUniversityMail(mail);

        if (accountRepository.existsByMail(mail))
            throw new AccountException(AccountErrorCode.DUPLICATE_MAIL);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RESEND_COOLDOWN_PREFIX + mail)))
            throw new AccountException(AccountErrorCode.TOO_FAST);

        String code = generateCode();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        // 메일을 받을 수신자 설정
        simpleMailMessage.setTo(mail);
        // 메일의 제목 설정
        simpleMailMessage.setSubject("[Passro] 이메일 인증 코드");
        // 메일의 내용 설정
        simpleMailMessage.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");

        asyncMailService.send(simpleMailMessage);

        stringRedisTemplate.opsForValue().set(CODE_PREFIX + mail, code, CODE_TTL);
        stringRedisTemplate.opsForValue().set(RESEND_COOLDOWN_PREFIX + mail, "true", RESEND_COOLDOWN_TTL);
    }

    private String generateCode(){
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendMailMessageAndEditPassword(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND));

        String mail = account.getMail();

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RESEND_COOLDOWN_PREFIX + mail)))
            throw new AccountException(AccountErrorCode.TOO_FAST);

        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(EDIT_PASSWORD_COOLDOWN_PREFIX + accountId)))
            throw new AccountException(AccountErrorCode.TOO_FAST);

        String code = generateCode();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        // 메일을 받을 수신자 설정
        simpleMailMessage.setTo(mail);
        // 메일의 제목 설정
        simpleMailMessage.setSubject("[Passro] 이메일 인증 코드");
        // 메일의 내용 설정
        simpleMailMessage.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");

        asyncMailService.send(simpleMailMessage);

        stringRedisTemplate.opsForValue().set(CODE_PREFIX + mail, code, CODE_TTL);
        stringRedisTemplate.opsForValue().set(RESEND_COOLDOWN_PREFIX + mail, "true", RESEND_COOLDOWN_TTL);
    }

    private void validateUniversityMail(String mail){
        int atIndex = mail.indexOf("@");
        if (atIndex == -1 || atIndex == mail.length() - 1)
            throw new AccountException(AccountErrorCode.INVALID_MAIL_DOMAIN);

        String domain = mail.substring(atIndex + 1).toLowerCase();

        boolean allowed = universityRepository.findAll().stream()
                .anyMatch(university -> {
                    String registered = university.getMailDomain().toLowerCase();
                    return domain.equals(registered) || domain.endsWith("." + registered);
                });

        if (!allowed)
            throw new AccountException(AccountErrorCode.INVALID_MAIL_DOMAIN);
    }

    public void confirmUniversityCode(AuthReqDTO.ConfirmCode dto, Long accountId){
        String mail = dto.getMail();
        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX+mail);

        savedCodeConfirm(code, savedCode);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountException(AccountErrorCode.NOT_FOUND));

        account.certify();

        accountRepository.save(account);

        stringRedisTemplate.delete(CODE_PREFIX + mail);
    }

    public void savedCodeConfirm(String code, String savedCode){
        if(savedCode==null)
            throw new AccountException(AccountErrorCode.MAIL_CODE_EXPIRED);
        if(!savedCode.equals(code))
            throw new AccountException(AccountErrorCode.MAIL_CODE_MISMATCH);
    }

    public void confirmCode(AuthReqDTO.ConfirmCode dto){
        String mail = dto.getMail();
        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX+mail);

        savedCodeConfirm(code, savedCode);

        stringRedisTemplate.delete(CODE_PREFIX + mail);
        stringRedisTemplate.opsForValue().set(VERIFIED_PREFIX + mail, "true", VERIFIED_TTL);

    }
}
