package com.passro.passrobackend.account.service;

import com.passro.passrobackend.account.dto.AuthDTO;
import com.passro.passrobackend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public void email(){}

    public void signup(AuthDTO.Signup dto){

    }

    private final JavaMailSender javaMailSender;

    public void sendMailMessage(String mail) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        try {
            // 메일을 받을 수신자 설정
            simpleMailMessage.setTo(mail
            );
            // 메일의 제목 설정
            simpleMailMessage.setSubject("테스트 메일 제목");
            // 메일의 내용 설정
            simpleMailMessage.setText("테스트 메일 내용");

            javaMailSender.send(simpleMailMessage);


        } catch (Exception e) {

            throw new RuntimeException(e);
        }

    }
}
