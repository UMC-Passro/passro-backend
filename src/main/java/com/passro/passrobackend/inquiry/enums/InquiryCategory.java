package com.passro.passrobackend.inquiry.enums;

/**
 * 문의 카테고리
 * TODO(#2): 팀에서 카테고리 값 확정 시 아래 임시 후보 값을 교체할 것.
 *           (@Enumerated(EnumType.STRING)으로 저장되므로 확정 전에 저장된 데이터가 있다면 값 변경 시 주의)
 */
public enum InquiryCategory {
    // 배송 지연
    DELAY,

    // 파손
    DAMAGE,

    // 분실
    LOST,

    // 오배송
    WRONG_DELIVERY,

    // 요금/포인트
    POINT,

    // 기타
    ETC
}
