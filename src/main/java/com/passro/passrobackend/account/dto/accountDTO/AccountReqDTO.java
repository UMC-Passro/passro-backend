package com.passro.passrobackend.account.dto.accountDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AccountReqDTO {

    @Getter
    public static class EditNickname {
        @NotBlank(message = "변경할 닉네임을 입력하세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]+$",
                message = "닉네임은 완성된 한글, 영문, 숫자만 입력 가능합니다."
        )
        private String nickname;
    }
}
