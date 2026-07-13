## 📌 관련 이슈

- Close #10

## ✨ 구현 내용

배송기사(Shipper)가 배송 요청을 조회하고 배송 상태를 변경할 수 있는 API를 구현했습니다.

### 배송 조회

- 매칭 대기 중인 배송 목록 조회
- 인증된 사용자 기준 배송 목록 조회
- 배송 ID를 이용한 상세 조회
- `Delivery` 엔티티를 응답용 `ShipperDeliveryDto`로 변환

### 배송 상태 변경

배송 진행 단계에 따라 상태를 변경하도록 구현했습니다.

```text
WAIT
  → MATCHED
  → DELIVERING
  → CONFIRM_REQUESTED
  → DELIVERED
```

- 배송 매칭 수락 시 배송기사 정보 설정 및 `MATCHED` 상태로 변경
- 물품 인수 시 `DELIVERING` 상태로 변경
- 배송 완료 후 검수 요청 시 `CONFIRM_REQUESTED` 상태로 변경
- 각 배송 상태의 의미를 `DeliveryState`에 주석으로 추가

### 예외 및 응답 처리

- 존재하지 않는 배송 조회 시 `DeliveryException` 발생
- 배송 조회 실패를 위한 `DeliveryErrorCode.NOT_FOUND` 추가
- 배송 및 Shipper API 성공 응답 코드 추가
- 공통 `APIResponse` 형식으로 응답 반환

### Repository

- 발송자 기준 배송 목록 조회 메서드 추가
- 배송 상태 기준 목록 조회 메서드 추가

## 🔌 API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/shipper/matched` | 매칭 대기 중인 배송 목록 조회 |
| `GET` | `/shipper/` | 인증된 사용자 기준 배송 목록 조회 |
| `GET` | `/shipper/{deliveryId}/` | 배송 상세 조회 |
| `PATCH` | `/shipper/{deliveryId}/matched` | 배송 매칭 수락 |
| `PATCH` | `/shipper/{deliveryId}/acquire` | 물품 인수 처리 |
| `PATCH` | `/shipper/{deliveryId}/confirm` | 배송 완료 후 검수 요청 |

## ✅ 확인 사항

- [ ] 매칭 대기 배송 목록 조회
- [ ] 사용자 기준 배송 목록 조회
- [ ] 배송 상세 조회
- [ ] 존재하지 않는 배송 조회 시 예외 응답
- [ ] 매칭 수락 시 배송기사 설정 및 상태 변경
- [ ] 물품 인수 시 배송 상태 변경
- [ ] 검수 요청 시 배송 상태 변경
