package com.passro.passrobackend.account.service;

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
import com.passro.passrobackend.global.jwt.JwtProperties;
import com.passro.passrobackend.global.jwt.JwtProvider;
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.place.repository.PlaceRepository;
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

    private static final String CODE_PREFIX = "email:verify:code:";
    private static final String VERIFIED_PREFIX = "email:verify:done:";
    private static final String COOLDOWN_PREFIX = "email:verify:cooldown:";
    private static final String REFRESH_PREFIX = "refresh:token:";
    private static final String TEMP_PASSWORD_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    public void sendMailMessage(AuthReqDTO.SendMail dto) {
        String mail = dto.getMail();

        if(dto.isStudent())
            validateUniversityEmail(mail);

        if (accountRepository.existsByEmail(mail))
            throw new AccountException(AccountErrorCode.DUPLICATE_EMAIL);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(COOLDOWN_PREFIX + mail)))
            throw new AccountException(AccountErrorCode.MAIL_RESEND_TOO_FAST);

        String code = generateCode();

        stringRedisTemplate.opsForValue().set(CODE_PREFIX + mail, code, CODE_TTL);
        stringRedisTemplate.opsForValue().set(COOLDOWN_PREFIX + mail, "true", RESEND_COOLDOWN);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        // 메일을 받을 수신자 설정
        simpleMailMessage.setTo(mail);
        // 메일의 제목 설정
        simpleMailMessage.setSubject("[Passro] 이메일 인증 코드");
        // 메일의 내용 설정
        simpleMailMessage.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");

        asyncMailService.send(simpleMailMessage);

    }

    public boolean isNicknameAvailable(String nickname) {
        return !accountRepository.existsByNickname(nickname);
    }

    private void validateUniversityEmail(String email){
        int atIndex = email.indexOf("@");
        if (atIndex == -1 || atIndex == email.length() - 1)
            throw new AccountException(AccountErrorCode.INVALID_EMAIL_DOMAIN);

        String domain = email.substring(atIndex + 1).toLowerCase();

        boolean allowed = universityRepository.findAll().stream()
                .anyMatch(university -> {
                    String registered = university.getEmailDomain().toLowerCase();
                    return domain.equals(registered) || domain.endsWith("." + registered);
                });

        if (!allowed)
            throw new AccountException(AccountErrorCode.INVALID_EMAIL_DOMAIN);
    }

    private String generateCode(){
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    public void confirmCode(AuthReqDTO.ConfirmCode dto){
        String mail = dto.getMail();
        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX+mail);

        savedCodeConfirm(mail, code, savedCode);

        stringRedisTemplate.delete(CODE_PREFIX + mail);
        stringRedisTemplate.opsForValue().set(VERIFIED_PREFIX + mail, "true", VERIFIED_TTL);

    }

    public void confirmUniversityCode(AuthReqDTO.ConfirmCode dto, Long accountId){
        String mail = dto.getMail();
        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX+mail);

        savedCodeConfirm(mail, code, savedCode);

        stringRedisTemplate.delete(CODE_PREFIX + mail);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountException(AccountErrorCode.NOT_FOUND));

        account.certify();
        accountRepository.save(account);

    }

    @Transactional
    public void signup(AuthReqDTO.Signup dto){
        String isConfirm = stringRedisTemplate.opsForValue().get(VERIFIED_PREFIX+dto.getEmail());

        if(isConfirm==null || !isConfirm.equals("true"))
            throw new AccountException(AccountErrorCode.MAIL_NOT_CONFIRM);

        if(accountRepository.existsByEmail(dto.getEmail()))
            throw new AccountException(AccountErrorCode.DUPLICATE_EMAIL);

        if(accountRepository.existsByNickname(dto.getNickname()))
            throw new AccountException(AccountErrorCode.DUPLICATE_NICKNAME);

        String password = passwordEncoder.encode(dto.getPassword());

        Place startPlace = placeRepository.findById(dto.getSourceStationId())
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY));
        Place destinationPlace = placeRepository.findById(dto.getDestinationStationId())
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND_SUBWAY));


        Account account = accountRepository.save(Account.builder()
                .email(dto.getEmail())
                .password(password)
                .nickname(dto.getNickname())
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
                .certified(true)
                .point(0L)
                .picture(dto.getPicture())
                .role(AccountRole.USER)
                .build());

        AccountPlace accountPlace = accountPlaceRepository.save(AccountPlace.builder()
                .account(account)
                .startPlace(startPlace)
                .destinationPlace(destinationPlace)
                .build());


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
        stringRedisTemplate.delete(VERIFIED_PREFIX + dto.getEmail());
    }

    public AuthResDTO.TokenResponse login(AuthReqDTO.Login dto){
        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(()->new AccountException(AccountErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(dto.getPassword(), account.getPassword()))
            throw new AccountException(AccountErrorCode.INVALID_CREDENTIALS);

        return issueTokens(account);
    }

    public void logout(Long accountId) {
        stringRedisTemplate.delete(REFRESH_PREFIX + accountId);
    }

    public void findId(AuthReqDTO.FindId dto) {
        accountRepository.findFirstByNameAndPhone(dto.getName(), dto.getPhone())
                .ifPresent(account -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(account.getEmail());
                    message.setSubject("[Passro] 아이디 찾기 안내");
                    message.setText("가입된 아이디(이메일): " + account.getEmail());
                    asyncMailService.send(message);
                });
    }

    @Transactional
    public void findPassword(AuthReqDTO.FindPassword dto) {
        accountRepository.findFirstByNameAndPhoneAndEmail(
                        dto.getName(), dto.getPhone(), dto.getEmail())
                .ifPresent(account -> {
                    String temporaryPassword = generateTemporaryPassword();
                    account.setPassword(passwordEncoder.encode(temporaryPassword));
                    accountRepository.save(account);

                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(account.getEmail());
                    message.setSubject("[Passro] 임시 비밀번호 안내");
                    message.setText("임시 비밀번호: " + temporaryPassword
                            + "\n로그인 후 비밀번호를 변경해주세요.");
                    asyncMailService.send(message);
                });
    }

    public AuthResDTO.TokenResponse reissueToken(AuthReqDTO.ReIssue dto){
        String refreshToken = dto.getRefreshToken();

        if(!jwtProvider.validateToken(refreshToken))
            throw new AccountException(AccountErrorCode.INVALID_REFRESH_TOKEN);

        Long accountId = jwtProvider.getAccountId(refreshToken);
        String savedToken = stringRedisTemplate.opsForValue().get(REFRESH_PREFIX + accountId);

        if(savedToken == null || !savedToken.equals(refreshToken))
            throw new AccountException(AccountErrorCode.INVALID_REFRESH_TOKEN);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.NOT_FOUND));

        return issueTokens(account);
    }

    private AuthResDTO.TokenResponse issueTokens(Account account){
        String accessToken = jwtProvider.createAccessToken(account.getId(), account.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(account.getId());

        stringRedisTemplate.opsForValue().set(REFRESH_PREFIX + account.getId(), refreshToken,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()));

        return new AuthResDTO.TokenResponse(accessToken, refreshToken);
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int index = 0; index < TEMP_PASSWORD_LENGTH; index++) {
            password.append(TEMP_PASSWORD_CHARACTERS.charAt(
                    SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }

    private void savedCodeConfirm(String mail, String code, String savedCode){
        if(savedCode==null)
            throw new AccountException(AccountErrorCode.MAIL_CODE_EXPIRED);
        if(!savedCode.equals(code)) {
            stringRedisTemplate.delete(CODE_PREFIX + mail);
            throw new AccountException(AccountErrorCode.MAIL_CODE_MISMATCH);
        }
    }
}
