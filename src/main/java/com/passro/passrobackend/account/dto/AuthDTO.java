package com.passro.passrobackend.account.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class AuthDTO {

    @Getter
    public class Signup{
        private String email;
        private String password;
        private String nickname;

        private String name;
        private String phoneNumber;
        private String birthDay;
        private String address;
    }

    @Getter
    public class SendMail{
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String mail;
    }
}
