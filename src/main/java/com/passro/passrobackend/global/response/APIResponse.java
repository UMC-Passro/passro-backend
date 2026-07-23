package com.passro.passrobackend.global.response;

import com.passro.passrobackend.global.code.BaseErrorCode;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "API 공통 응답")
public class APIResponse<T> {
    @Schema(description = "요청 성공 여부", example = "false")
    private final boolean isSuccess;

    @Schema(description = "서비스 응답 코드", example = "DELIVERY404_1")
    private final String code;

    @Schema(description = "응답 메시지", example = "배송 정보를 찾을 수 없습니다.")
    private final String message;

    @Schema(description = "응답 데이터. 오류에 따라 필드별 검증 오류가 포함되거나 null입니다.", nullable = true)
    private T result;

    public static <T> APIResponse<T> onSuccess(BaseSuccessCode code, T result){
        return new APIResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static <T> APIResponse<T> onFailure(BaseErrorCode code, T result){
        return new APIResponse<>(false, code.getCode(), code.getMessage(), result);
    }
}
