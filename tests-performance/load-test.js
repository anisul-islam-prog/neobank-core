/**
 * NeoBank Performance Load Test
 * 
 * Simulates 200 concurrent users performing:
 * Login -> Check Balance -> Transfer flow
 * 
 * Thresholds:
 * - 95% of requests must be < 500ms
 * - Failure rate must be < 1%
 * 
 * Usage:
 *   k6 run load-test.js
 *   k6 run --vus 100 --duration 5m load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics for business flows
const moneyFlowSuccess = new Rate('money_flow_success');
const loginSuccess = new Rate('login_success');
const transferSuccess = new Rate('transfer_success');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp up to 50 users
    { duration: '1m', target: 200 },   // Ramp up to 200 users
    { duration: '3m', target: 200 },   // Stay at 200 users
    { duration: '30s', target: 0 },    // Ramp down to 0
  ],
  thresholds: {
    // Overall thresholds
    'http_req_duration': ['p(95)<500'],  // 95% of requests < 500ms
    'http_req_failed': ['rate<0.01'],    // Failure rate < 1%
    
    // Custom metric thresholds
    'money_flow_success': ['rate>0.99'],  // 99% money flows succeed
    'login_success': ['rate>0.99'],       // 99% logins succeed
    'transfer_success': ['rate>0.99'],    // 99% transfers succeed
    
    // Additional performance thresholds
    'http_req_duration{expected_response:true}': ['p(95)<500'],
    'http_req_duration{expected_response:false}': ['p(95)<1000'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

// Environment configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_USERNAME_PREFIX = __ENV.TEST_USERNAME_PREFIX || 'loadtest_user';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'LoadTest123!';

// Test data pool (simulating different users)
const testUsers = [];
for (let i = 1; i <= 200; i++) {
  testUsers.push({
    username: `${TEST_USERNAME_PREFIX}${i}`,
    password: TEST_PASSWORD,
    email: `loadtest${i}@neobank.com`
  });
}

/**
 * Main load test scenario
 */
export default function () {
  // Pick a random test user
  const user = testUsers[Math.floor(Math.random() * testUsers.length)];
  
  // Step 1: Login
  const loginResult = login(user.username, user.password);
  
  if (!loginResult.success) {
    loginSuccess.add(0);
    moneyFlowSuccess.add(0);
    return;
  }
  
  loginSuccess.add(1);
  sleep(1); // Think time between operations
  
  // Step 2: Check Balance
  const balanceResult = checkBalance(loginResult.token);
  sleep(1);
  
  // Step 3: Transfer (if we have accounts)
  if (balanceResult.fromAccountId && balanceResult.toAccountId) {
    const transferResult = transfer(
      loginResult.token,
      balanceResult.fromAccountId,
      balanceResult.toAccountId,
      1.00 // Small amount for load test
    );
    
    transferSuccess.add(transferResult.success ? 1 : 0);
    moneyFlowSuccess.add(transferResult.success ? 1 : 0);
  } else {
    transferSuccess.add(0);
    moneyFlowSuccess.add(0);
  }
  
  sleep(2); // Think time before next iteration
}

/**
 * Login and return JWT token
 */
function login(username, password) {
  const payload = {
    username: username,
    password: password
  };
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  const response = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify(payload), params);
  
  const success = check(response, {
    'login status is 200': (r) => r.status === 200,
    'login returns token': (r) => JSON.parse(r.body).token !== undefined,
  });
  
  let token = null;
  if (success) {
    try {
      token = JSON.parse(response.body).token;
    } catch (e) {
      console.error('Failed to parse login response');
    }
  }
  
  return { success, token };
}

/**
 * Check account balance
 */
function checkBalance(token) {
  const params = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };
  
  const response = http.get(`${BASE_URL}/api/accounts`, params);
  
  check(response, {
    'accounts status is 200': (r) => r.status === 200,
  });
  
  // Try to extract account IDs for transfer test
  let fromAccountId = null;
  let toAccountId = null;
  
  try {
    const accounts = JSON.parse(response.body);
    if (accounts && accounts.length >= 2) {
      fromAccountId = accounts[0].id;
      toAccountId = accounts[1].id;
    }
  } catch (e) {
    // Ignore parsing errors
  }
  
  return { 
    success: response.status === 200,
    fromAccountId,
    toAccountId
  };
}

/**
 * Execute a fund transfer
 */
function transfer(token, fromAccountId, toAccountId, amount) {
  const payload = {
    fromId: fromAccountId,
    toId: toAccountId,
    amount: amount,
    currency: 'USD'
  };
  
  const params = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };
  
  const response = http.post(`${BASE_URL}/api/transfers`, JSON.stringify(payload), params);
  
  const success = check(response, {
    'transfer status is 200': (r) => r.status === 200,
    'transfer succeeds': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.status === 'success';
      } catch (e) {
        return false;
      }
    },
  });
  
  return { success };
}

/**
 * Handle test teardown
 */
export function handleSummary(data) {
  const summary = {
    timestamp: new Date().toISOString(),
    test: 'NeoBank Performance Load Test',
    thresholds: {
      'p95_latency_ms': 500,
      'max_failure_rate': 0.01,
      'min_money_flow_success': 0.99,
    },
    results: {
      requests_total: data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0,
      failures_total: data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate * 100 : 0,
      p95_latency_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] : 0,
      avg_latency_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values.avg : 0,
      max_latency_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values.max : 0,
      money_flow_success_rate: data.metrics.money_flow_success ? data.metrics.money_flow_success.values.rate * 100 : 0,
      login_success_rate: data.metrics.login_success ? data.metrics.login_success.values.rate * 100 : 0,
      transfer_success_rate: data.metrics.transfer_success ? data.metrics.transfer_success.values.rate * 100 : 0,
    },
    passed: {
      latency_threshold: (data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] : 9999) < 500,
      failure_threshold: (data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate : 1) < 0.01,
      money_flow_threshold: (data.metrics.money_flow_success ? data.metrics.money_flow_success.values.rate : 0) > 0.99,
    }
  };
  
  // Console output
  console.log('\n========================================');
  console.log('   NeoBank Load Test Results');
  console.log('========================================');
  console.log(`Total Requests: ${summary.results.requests_total}`);
  console.log(`P95 Latency: ${summary.results.p95_latency_ms.toFixed(2)}ms (threshold: 500ms)`);
  console.log(`Failure Rate: ${summary.results.failures_total.toFixed(2)}% (threshold: 1%)`);
  console.log(`Money Flow Success: ${summary.results.money_flow_success_rate.toFixed(2)}% (threshold: 99%)`);
  console.log(`Login Success: ${summary.results.login_success_rate.toFixed(2)}%`);
  console.log(`Transfer Success: ${summary.results.transfer_success_rate.toFixed(2)}%`);
  console.log('----------------------------------------');
  console.log(`Latency Test: ${summary.passed.latency_threshold ? '✅ PASSED' : '❌ FAILED'}`);
  console.log(`Failure Rate Test: ${summary.passed.failure_threshold ? '✅ PASSED' : '❌ FAILED'}`);
  console.log(`Money Flow Test: ${summary.passed.money_flow_threshold ? '✅ PASSED' : '❌ FAILED'}`);
  console.log('========================================\n');
  
  return {
    'stdout': textSummary(summary),
    [`results/load-test-${Date.now()}.json`]: JSON.stringify(summary, null, 2),
  };
}

function textSummary(summary) {
  return `
NeoBank Load Test Summary
=========================
Timestamp: ${summary.timestamp}
Total Requests: ${summary.results.requests_total}
P95 Latency: ${summary.results.p95_latency_ms.toFixed(2)}ms
Failure Rate: ${summary.results.failures_total.toFixed(2)}%
Money Flow Success: ${summary.results.money_flow_success_rate.toFixed(2)}%

Thresholds:
- Latency (<500ms): ${summary.passed.latency_threshold ? '✅ PASS' : '❌ FAIL'}
- Failure Rate (<1%): ${summary.passed.failure_threshold ? '✅ PASS' : '❌ FAIL'}
- Money Flow (>99%): ${summary.passed.money_flow_threshold ? '✅ PASS' : '❌ FAIL'}
`;
}
