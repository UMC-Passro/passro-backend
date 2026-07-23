package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.AuthDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.passro.passrobackend.global.configuration.SwaggerErrorExamples.*;
import static com.passro.passrobackend.global.configuration.SwaggerSuccessExamples.ACCOUNT_OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증", description = "이메일 인증 및 회원가입 API")
public class AuthController {

    private final AccountService accountService;



    @PostMapping("/mail/send")
    @Operation(summary = "인증 메일 발송", description = "대학교 이메일로 6자리 인증 코드를 발송합니다. 인증 코드는 5분 동안 유효합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 메일 발송 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "ACCOUNT200_1", summary = "인증 메일 발송 성공", value = ACCOUNT_OK))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 또는 이미 가입된 이메일",
                    content = @Content(schema = @Schema(implementation = APIResponse.class), examples = {
                            @ExampleObject(name = "COMMON400", summary = "요청 값 검증 실패", value = COMMON_VALIDATION),
                            @ExampleObject(name = "ACCOUNT400_4", summary = "이메일 중복", value = ACCOUNT_DUPLICATE_EMAIL),
                            @ExampleObject(name = "ACCOUNT400_6", summary = "대학교 이메일이 아님", value = ACCOUNT_INVALID_EMAIL_DOMAIN)
                    })),
            @ApiResponse(responseCode = "429", description = "인증 메일 재발송 제한",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "ACCOUNT429_1", summary = "인증 메일 재발송 제한", value = ACCOUNT_MAIL_RESEND_TOO_FAST)))
    })
    public APIResponse<Void> mailSend(@Valid @RequestBody AuthDTO.SendMail dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessage(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/mail/confirm")
    @Operation(summary = "이메일 인증 코드 확인", description = "이메일로 발송된 6자리 인증 코드를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "ACCOUNT200_1", summary = "이메일 인증 성공", value = ACCOUNT_OK))),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료",
                    content = @Content(schema = @Schema(implementation = APIResponse.class), examples = {
                            @ExampleObject(name = "COMMON400", summary = "요청 값 검증 실패", value = COMMON_VALIDATION),
                            @ExampleObject(name = "ACCOUNT400_1", summary = "인증 코드 만료", value = ACCOUNT_MAIL_CODE_EXPIRED),
                            @ExampleObject(name = "ACCOUNT400_2", summary = "인증 코드 불일치", value = ACCOUNT_MAIL_CODE_MISMATCH)
                    }))
    })
    public APIResponse<Void> confirmCode(@Valid @RequestBody AuthDTO.ConfirmCode dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.confirmCode(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일 인증을 완료한 사용자의 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "ACCOUNT200_1", summary = "회원가입 성공", value = ACCOUNT_OK))),
            @ApiResponse(responseCode = "400", description = "이메일 미인증, 이메일 중복 또는 닉네임 중복",
                    content = @Content(schema = @Schema(implementation = APIResponse.class), examples = {
                            @ExampleObject(name = "COMMON400", summary = "요청 값 검증 실패", value = COMMON_VALIDATION),
                            @ExampleObject(name = "ACCOUNT400_3", summary = "이메일 미인증", value = ACCOUNT_MAIL_NOT_CONFIRMED),
                            @ExampleObject(name = "ACCOUNT400_4", summary = "이메일 중복", value = ACCOUNT_DUPLICATE_EMAIL),
                            @ExampleObject(name = "ACCOUNT400_5", summary = "닉네임 중복", value = ACCOUNT_DUPLICATE_NICKNAME)
                    }))
    })
    public APIResponse<Void> signup(@Valid @RequestBody AuthDTO.Signup dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.signup(dto);
        return APIResponse.onSuccess(code, null);
    }
}
