package com.passro.passrobackend.account.dto.authDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class AuthReqDTO {

    @Getter
    public static class Signup{
        private String email;
        private String password;
        private String nickname;
        private String name;
        private String phone;
        private LocalDate birth;
        private Long point;
        private String picture;
        private String startStationName;
        private String startRouteName;
        private String destinationStationName;
        private String destinationRouteName;
        private List<WayPoint> wayPoints;
    }

    @Getter
    public static class mypage{

    }

    @Getter
    public static class WayPoint{
        private String stationName;
        private String routeName;
    }


    @Getter
    public static class SendMail{
        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String mail;

        private boolean student;
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

    @Getter
    public static class ReIssue{

        @NotBlank
        private String refreshToken;
    }
}
