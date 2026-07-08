package com.passro.passrobackend.file.exception.code;

import com.passro.passrobackend.global.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum FileErrorCode implements BaseErrorCode {
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE404_1", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE500_1", "파일 업로드 주소를 가져오는 데 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
