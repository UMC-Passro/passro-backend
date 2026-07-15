package com.passro.passrobackend.account.dto;

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

    public class SendMail{
        private String mail;
        private String code;
    }
}
