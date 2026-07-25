package com.passro.passrobackend.account.dto;

import com.passro.passrobackend.place.entity.Place;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;

public class AuthReqDTO {

    @Getter
    public static class Signup{
        private String email;
        private String password;
        private String nickname;

        private Place place_id;

        private String name;
        private String phone;
        private LocalDate birth;
        private Long point;
        private String picture;
    }

    @Getter
    public static class SendMail{
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String mail;
    }

    @Getter
    public static class ConfirmCode{
        private String mail;

        @NotBlank(message="인증 코드를 입력하세요")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 숫자 6자리여야 합니다.")
        private String code;
    }

    @Getter
    public static class Login {

        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호를 입력하세요.")
        private String password;
    }
}
