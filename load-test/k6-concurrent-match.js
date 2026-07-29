import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const successCounter = new Counter('successful_matches');
const conflictCounter = new Counter('conflict_matches');

export const options = {
  scenarios: {
    concurrent_matching: {
      executor: 'per-vu-iterations',
      vus: 10,             // 10명의 배송기사가 동시에 수락 요청
      iterations: 1,      // VU당 1회 실행
      maxDuration: '10s',
    },
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TARGET_DELIVERY_ID = __ENV.DELIVERY_ID || '1';

// Setup: 가상 기사 계정별 토큰 생성 (실제 환경에서는 멀티 계정 준비)
export function setup() {
  // 실제 테스트 시 여러 기사 계정의 토큰 배열을 구성하거나 단일 환경에 맞춰 설정
  return {
    deliveryId: TARGET_DELIVERY_ID,
  };
}

export default function (data) {
  // 환경변수로 전달받거나 setup에서 설정된 기사 토큰 사용
  const shipperToken = __ENV[`SHIPPER_TOKEN_${__VU}`] || __ENV.SHIPPER_TOKEN || 'dummy-token';

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${shipperToken}`,
    },
  };

  // 동시에 동일 배송건 수락 요청
  const res = http.patch(`${BASE_URL}/shipper/${data.deliveryId}/matched`, null, params);

  if (res.status === 200) {
    successCounter.add(1);
    console.log(`VU ${__VU}: 매칭 수락 성공! (200 OK)`);
  } else if (res.status === 400) {
    conflictCounter.add(1);
    console.log(`VU ${__VU}: 이미 매칭된 배송건 (400 Bad Request)`);
  }

  check(res, {
    'status is 200 or 400': (r) => r.status === 200 || r.status === 400,
  });
}
