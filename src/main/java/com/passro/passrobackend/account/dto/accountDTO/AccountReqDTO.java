package com.passro.passrobackend.account.dto.accountDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

public class AccountReqDTO {

    @Getter
    public static class EditMyInfo {
        @NotBlank(message = "변경할 닉네임을 입력하세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]+$",
                message = "닉네임은 완성된 한글, 영문, 숫자만 입력 가능합니다."
        )
        private String nickname;

        @NotBlank(message = "변경할 전화번호를 입력하세요.")
        @Pattern(
                regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        private String phoneNumber;


    }

    @Getter
    public static class EditPassword {
        @NotBlank(message = "변경할 비밀번호를 입력하세요.")
        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "비밀번호는 영문, 숫자만 입력 가능합니다."
        )
        private String password;

        @NotBlank(message="인증 코드를 입력하세요")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 숫자 6자리여야 합니다.")
        private String code;
    }
}
