package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.AuthDTO;
import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.account.enums.AccountRole;
import com.passro.passrobackend.account.exception.AccountException;
import com.passro.passrobackend.account.exception.code.AccountErrorCode;
import com.passro.passrobackend.account.repository.AccountRepository;
import com.passro.passrobackend.account.repository.UniversityRepository;
import com.passro.passrobackend.place.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CODE_PREFIX = "email:verify:code:";
    private static final String VERIFIED_PREFIX = "email:verify:done:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    public void sendMailMessage(AuthDTO.SendMail dto) {
        String mail = dto.getMail();

        validateUniversityEmail(mail);

        String code = generateCode();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        stringRedisTemplate.opsForValue().set(CODE_PREFIX + mail, code, CODE_TTL);

        // 메일을 받을 수신자 설정
        simpleMailMessage.setTo(mail);
        // 메일의 제목 설정
        simpleMailMessage.setSubject("[Passro] 이메일 인증 코드");
        // 메일의 내용 설정
        simpleMailMessage.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");

        javaMailSender.send(simpleMailMessage);

    }

    private void validateUniversityEmail(String email){
        int atIndex = email.indexOf("@");
        if(atIndex == -1 || atIndex == email.length()-1)
            throw new AccountException(AccountErrorCode.INVALID_EMAIL_DOMAIN);

        String domain = email.substring(atIndex+1);
        if(!universityRepository.existsByEmailDomain(domain))
            throw new AccountException(AccountErrorCode.INVALID_EMAIL_DOMAIN);
    }

    private String generateCode(){
        int code = 100000 + ThreadLocalRandom.current().nextInt(900000);
        return String.valueOf(code);
    }

    public void confirmCode(AuthDTO.ConfirmCode dto){
        String mail = dto.getMail();
        String code = dto.getCode();

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX+mail);

        if(savedCode==null)
            throw new AccountException(AccountErrorCode.MAIL_CODE_EXPIRED);
        if(!savedCode.equals(code))
            throw new AccountException(AccountErrorCode.MAIL_CODE_MISMATCH);

        stringRedisTemplate.delete(CODE_PREFIX + mail);
        stringRedisTemplate.opsForValue().set(VERIFIED_PREFIX + mail, "true", VERIFIED_TTL);
    }

    public void signup(AuthDTO.Signup dto){
        String isConfirm = stringRedisTemplate.opsForValue().get(VERIFIED_PREFIX+dto.getEmail());

        if(isConfirm==null || !isConfirm.equals("true"))
            throw new AccountException(AccountErrorCode.MAIL_NOT_CONFIRM);

        if(accountRepository.existsByEmail(dto.getEmail()))
            throw new AccountException(AccountErrorCode.DUPLICATE_EMAIL);

        if(accountRepository.existsByNickname(dto.getNickname()))
            throw new AccountException(AccountErrorCode.DUPLICATE_NICKNAME);

        String password = passwordEncoder.encode(dto.getPassword());

        accountRepository.save(Account.builder()
                .email(dto.getEmail())
                .password(password)
                .nickname(dto.getNickname())
                .place_id(dto.getPlace_id())
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
                .certified(true)
                .point(0L)
                .picture(dto.getPicture())
                .role(AccountRole.USER)
                .build());

        stringRedisTemplate.delete(VERIFIED_PREFIX + dto.getEmail());
    }
}
