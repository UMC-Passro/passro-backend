import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// Custom Metrics
const matchingDuration = new Trend('matching_api_duration');
const successRate = new Rate('matching_success_rate');

// Test Configuration
export const options = {
  stages: [
    { duration: '10s', target: 10 },  // Ramp-up to 10 VUs in 10s
    { duration: '30s', target: 50 },  // Stay at 50 VUs for 30s
    { duration: '10s', target: 0 },   // Ramp-down to 0 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should respond in < 500ms
    http_req_failed: ['rate<0.01'],    // Error rate should be less than 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Setup: 로그인하여 테스트용 JWT 토큰 발급
export function setup() {
  const loginPayload = JSON.stringify({
    email: __ENV.TEST_EMAIL || 'shipper@example.com',
    password: __ENV.TEST_PASSWORD || 'password123!',
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post(`${BASE_URL}/auth/login`, loginPayload, params);

  if (res.status !== 200) {
    console.error(`Login failed with status ${res.status}: ${res.body}`);
    return { token: null };
  }

  const body = JSON.parse(res.body);
  const token = body.result.accessToken;
  return { token };
}

export default function (data) {
  if (!data.token) {
    console.error('No Auth token available. Skipping test iteration.');
    return;
  }

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${data.token}`,
    },
  };

  // 매칭 대기 배송 목록 조회 (우선순위/권역 필터링 포함 연산)
  const startTime = Date.now();
  const res = http.get(`${BASE_URL}/shipper/matched`, params);
  const duration = Date.now() - startTime;

  matchingDuration.add(duration);

  const isSuccess = check(res, {
    'status is 200': (r) => r.status === 200,
    'code is SHIPPER200_1': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.code === 'SHIPPER200_1' || body.isSuccess === true;
      } catch (e) {
        return false;
      }
    },
  });

  successRate.add(isSuccess);

  sleep(1); // 가상 사용자 요청 간격 (Think time)
}
