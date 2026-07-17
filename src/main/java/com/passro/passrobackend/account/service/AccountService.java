package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.AuthDTO;
import com.passro.passrobackend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate StringRedisTemplate;
    private final Random random;

    private static final String CODE_PREFIX = "email:verify:code:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    public void signup(AuthDTO.Signup dto){

    }

    public void sendMailMessage(AuthDTO.SendMail dto) {
        String mail = dto.getMail();
        String code = generateCode();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        StringRedisTemplate.opsForValue().set(CODE_PREFIX + mail, code, CODE_TTL);

        // 메일을 받을 수신자 설정
        simpleMailMessage.setTo(mail);
        // 메일의 제목 설정
        simpleMailMessage.setSubject("[Passro] 이메일 인증 코드");
        // 메일의 내용 설정
        simpleMailMessage.setText("인증 코드: " + code + "\n5분 이내에 입력해주세요.");

        javaMailSender.send(simpleMailMessage);

    }

    private String generateCode(){
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code); // 100000 ~ 999999 return
    }
}
