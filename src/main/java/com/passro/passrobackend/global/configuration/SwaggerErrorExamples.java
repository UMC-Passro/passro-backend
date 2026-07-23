package com.passro.passrobackend.global.configuration;

public final class SwaggerErrorExamples {

    private SwaggerErrorExamples() {
    }

    public static final String COMMON_VALIDATION =
            "{\"success\":false,\"code\":\"COMMON400\",\"message\":\"잘못된 요청\",\"result\":{\"field\":\"검증 오류 메시지\"}}";

    public static final String ACCOUNT_MAIL_CODE_EXPIRED =
            "{\"success\":false,\"code\":\"ACCOUNT400_1\",\"message\":\"인증 코드가 만료되었거나 존재하지 않습니다.\",\"result\":null}";
    public static final String ACCOUNT_MAIL_CODE_MISMATCH =
            "{\"success\":false,\"code\":\"ACCOUNT400_2\",\"message\":\"인증 코드가 일치하지 않습니다.\",\"result\":null}";
    public static final String ACCOUNT_MAIL_NOT_CONFIRMED =
            "{\"success\":false,\"code\":\"ACCOUNT400_3\",\"message\":\"인증되지 않은 이메일입니다.\",\"result\":null}";
    public static final String ACCOUNT_DUPLICATE_EMAIL =
            "{\"success\":false,\"code\":\"ACCOUNT400_4\",\"message\":\"이미 사용 중인 이메일입니다.\",\"result\":null}";
    public static final String ACCOUNT_DUPLICATE_NICKNAME =
            "{\"success\":false,\"code\":\"ACCOUNT400_5\",\"message\":\"이미 사용 중인 닉네임입니다.\",\"result\":null}";
    public static final String ACCOUNT_INVALID_EMAIL_DOMAIN =
            "{\"success\":false,\"code\":\"ACCOUNT400_6\",\"message\":\"학생 이메일이 아닙니다.\",\"result\":null}";
    public static final String ACCOUNT_MAIL_RESEND_TOO_FAST =
            "{\"success\":false,\"code\":\"ACCOUNT429_1\",\"message\":\"잠시 후 다시 시도해 주세요.\",\"result\":null}";

    public static final String DELIVERY_NOT_FOUND =
            "{\"success\":false,\"code\":\"DELIVERY404_1\",\"message\":\"해당 배송을 찾을 수 없습니다.\",\"result\":null}";
    public static final String DELIVERY_FORBIDDEN =
            "{\"success\":false,\"code\":\"DELIVERY403_1\",\"message\":\"해당 배송에 대한 접근 권한이 없습니다.\",\"result\":null}";
    public static final String DELIVERY_CANNOT_CANCEL =
            "{\"success\":false,\"code\":\"DELIVERY400_1\",\"message\":\"매칭이 진행된 배송은 취소할 수 없습니다.\",\"result\":null}";
    public static final String DELIVERY_INVALID_COMPLETION_STATUS =
            "{\"success\":false,\"code\":\"DELIVERY400_2\",\"message\":\"배송 완료 처리할 수 없는 상태입니다.\",\"result\":null}";

    public static final String FILE_NOT_FOUND =
            "{\"success\":false,\"code\":\"FILE404_1\",\"message\":\"파일을 찾을 수 없습니다.\",\"result\":null}";
    public static final String FILE_UPLOAD_FAILED =
            "{\"success\":false,\"code\":\"FILE500_1\",\"message\":\"파일 업로드 주소 발급에 실패했습니다.\",\"result\":null}";

    public static final String REVIEW_DELIVERY_NOT_FOUND =
            "{\"success\":false,\"code\":\"REVIEW404_1\",\"message\":\"해당 배송 건을 찾을 수 없습니다.\",\"result\":null}";
    public static final String REVIEW_ACCOUNT_NOT_FOUND =
            "{\"success\":false,\"code\":\"REVIEW404_2\",\"message\":\"해당 사용자를 찾을 수 없습니다.\",\"result\":null}";
    public static final String REVIEW_FORBIDDEN =
            "{\"success\":false,\"code\":\"REVIEW403_1\",\"message\":\"해당 배송 건의 발송자만 리뷰를 작성할 수 있습니다.\",\"result\":null}";
    public static final String REVIEW_DELIVERY_NOT_COMPLETED =
            "{\"success\":false,\"code\":\"REVIEW400_1\",\"message\":\"배송 완료 건에 대해서만 리뷰를 작성할 수 있습니다.\",\"result\":null}";
    public static final String REVIEW_ALREADY_EXISTS =
            "{\"success\":false,\"code\":\"REVIEW400_2\",\"message\":\"해당 배송 건에는 이미 리뷰가 작성되었습니다.\",\"result\":null}";
    public static final String REVIEW_INVALID_RATING =
            "{\"success\":false,\"code\":\"REVIEW400_3\",\"message\":\"평점은 1점 이상 5점 이하로 입력해야 합니다.\",\"result\":null}";
    public static final String REVIEW_INVALID_DELIVERY_ID =
            "{\"success\":false,\"code\":\"REVIEW400_4\",\"message\":\"deliveryId는 필수입니다.\",\"result\":null}";
}
