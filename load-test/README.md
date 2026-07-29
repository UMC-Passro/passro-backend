# 🚀 Passro Backend 부하 테스트 (Load Testing) 가이드

`passro-backend` 프로젝트의 주요 API 성능 및 동시성 검증을 위한 부하 테스트 스크립트 모음입니다.

---

## 🛠️ 1. k6 설치 방법

### Windows (winget 또는 choco)
```powershell
winget install k6 --source winget
# 또는
choco install k6
```

### Mac (Homebrew)
```bash
brew install k6
```

---

## 🏃 2. 부하 테스트 실행 방법

### (1) 매칭 대기 배송 목록 조회 부하 테스트 (`k6-shipper-matching.js`)
배송기사의 권역/동선 경로 연산 및 5단계 우선순위 정렬이 적용된 `GET /shipper/matched` API의 TPS 및 Latency(응답 지연시간)를 측정합니다.

```powershell
# 기본 실행 (http://localhost:8080)
k6 run load-test/k6-shipper-matching.js

# 테스트 계정 지정 및 대상 URL 변경 실행
k6 run -e BASE_URL=http://localhost:8080 -e TEST_EMAIL=shipper@example.com -e TEST_PASSWORD=password123! load-test/k6-shipper-matching.js
```

**주요 성능 목표 (Thresholds):**
- `p(95)`: 95%의 요청이 500ms 이내에 응답해야 함.
- `http_req_failed`: 요청 에러율 1% 미만 유지.

---

### (2) 동시 매칭 수락 락 경쟁 테스트 (`k6-concurrent-match.js`)
동일한 배송 ID(`PATCH /shipper/{deliveryId}/matched`)에 대해 여러 명의 배송기사가 동시에 수락 요청을 보낼 때, 1명만 수락에 성공하고 나머지는 안전하게 차단(400 Bad Request)되는지 검증합니다.

```powershell
k6 run -e DELIVERY_ID=1 -e SHIPPER_TOKEN=your_jwt_access_token load-test/k6-concurrent-match.js
```

---

## 📊 3. 부하 테스트 리포트 확인

k6 테스트 완료 후 터미널 콘솔에 다음과 같은 메트릭이 출력됩니다:
- `http_req_duration`: 요청 처리 소요시간 (avg, min, med, max, p90, p95)
- `http_reqs`: 초당 처리량 (RPS / TPS)
- `matching_success_rate`: API 성공률
