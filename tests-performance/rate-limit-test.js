/**
 * NeoBank Rate Limit Validation Test
 * 
 * Tests the Bucket4j rate limiting filter by sending
 * 20 requests per second to the registration endpoint.
 * 
 * Expected behavior:
 * - First 5 requests should succeed (200 or 400 for validation errors)
 * - Subsequent requests should return 429 Too Many Requests
 * 
 * Usage:
 *   k6 run rate-limit-test.js
 *   k6 run --vus 10 --duration 1m rate-limit-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const rateLimitTriggered = new Rate('rate_limit_triggered');
const requestsBeforeLimit = new Trend('requests_before_limit');
const responseTimeBeforeLimit = new Trend('response_time_before_limit');

// Test configuration
export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    // Rate limiting should trigger for most requests
    'rate_limit_triggered': ['rate>0.7'],  // At least 70% should be rate limited
    'http_req_duration': ['p(95)<1000'],   // Even rate-limited requests should be fast
  },
};

// Environment configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * Counter for tracking requests per VU
 */
let requestCount = 0;
let successCount = 0;
let rateLimitedCount = 0;

/**
 * Main test scenario - hammer the registration endpoint
 */
export default function () {
  requestCount++;
  
  // Generate unique user data for each request
  const timestamp = Date.now();
  const payload = {
    username: `ratelimit_test_${timestamp}_${requestCount}`,
    email: `ratelimit${timestamp}_${requestCount}@neobank.com`,
    password: 'RateLimitTest123!'
  };
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  const response = http.post(`${BASE_URL}/api/onboarding/register`, JSON.stringify(payload), params);
  
  // Check for rate limit response
  const isRateLimited = response.status === 429;
  const isSuccess = response.status === 200 || response.status === 400; // 400 is validation error, still "success"
  
  if (isRateLimited) {
    rateLimitedCount++;
    rateLimitTriggered.add(1);
  } else if (isSuccess) {
    successCount++;
    rateLimitTriggered.add(0);
    requestsBeforeLimit.add(requestCount);
    responseTimeBeforeLimit.add(response.timings.duration);
  }
  
  // Validate rate limit headers when present
  check(response, {
    'has rate limit headers when 429': (r) => {
      if (r.status !== 429) return true; // Only check if rate limited
      return r.headers['X-RateLimit-Limit'] !== undefined &&
             r.headers['X-RateLimit-Remaining'] !== undefined &&
             r.headers['X-RateLimit-Reset'] !== undefined;
    },
    'rate limit remaining is 0 when 429': (r) => {
      if (r.status !== 429) return true;
      return r.headers['X-RateLimit-Remaining'] === '0';
    },
    'rate limit message present when 429': (r) => {
      if (r.status !== 429) return true;
      try {
        const body = JSON.parse(r.body);
        return body.error !== undefined && body.error.includes('Too many');
      } catch (e) {
        return false;
      }
    },
  });
  
  // Small delay to simulate realistic request pattern
  sleep(0.05); // 50ms between requests per VU = ~20 req/s total
}

/**
 * Setup - verify endpoint is accessible before test
 */
export function setup() {
  console.log('Rate Limit Test Setup');
  console.log('=====================');
  console.log(`Target URL: ${BASE_URL}/api/onboarding/register`);
  console.log(`Expected limit: 5 requests per minute`);
  console.log(`Test duration: ${options.duration}`);
  console.log(`Virtual users: ${options.vus}`);
  console.log('');
  
  // Quick health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    console.warn('⚠️  Warning: Backend may not be healthy');
  }
  
  return { startTime: Date.now() };
}

/**
 * Teardown - print summary
 */
export function teardown(data) {
  const duration = (Date.now() - data.startTime) / 1000;
  const totalRequests = successCount + rateLimitedCount;
  const rateLimitPercentage = (rateLimitedCount / totalRequests) * 100;
  
  console.log('\n========================================');
  console.log('   Rate Limit Test Results');
  console.log('========================================');
  console.log(`Test Duration: ${duration.toFixed(2)}s`);
  console.log(`Total Requests: ${totalRequests}`);
  console.log(`Successful (before limit): ${successCount}`);
  console.log(`Rate Limited (429): ${rateLimitedCount}`);
  console.log(`Rate Limit Triggered: ${rateLimitPercentage.toFixed(2)}%`);
  console.log(`Avg Requests Before Limit: ${requestsBeforeLimit.avg.toFixed(2)}`);
  console.log(`Avg Response Time (before limit): ${responseTimeBeforeLimit.avg.toFixed(2)}ms`);
  console.log('----------------------------------------');
  
  // Verify rate limiting worked
  if (rateLimitPercentage > 70) {
    console.log('✅ Rate limiting is working correctly!');
    console.log(`   ${rateLimitPercentage.toFixed(2)}% of requests were rate limited`);
  } else {
    console.log('❌ Rate limiting may not be working as expected');
    console.log(`   Only ${rateLimitPercentage.toFixed(2)}% of requests were rate limited`);
    console.log('   Expected: >70%');
  }
  
  console.log('========================================\n');
}

/**
 * Handle test summary
 */
export function handleSummary(data) {
  const summary = {
    timestamp: new Date().toISOString(),
    test: 'NeoBank Rate Limit Validation',
    endpoint: `${BASE_URL}/api/onboarding/register`,
    expected_limit: '5 requests per minute',
    results: {
      total_requests: data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0,
      rate_limited_count: rateLimitedCount,
      success_count: successCount,
      rate_limit_percentage: rateLimitedCount / (successCount + rateLimitedCount) * 100,
      avg_requests_before_limit: requestsBeforeLimit.avg,
      avg_response_time_ms: responseTimeBeforeLimit.avg,
      p95_response_time_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] : 0,
    },
    thresholds: {
      min_rate_limit_percentage: 70,
      max_p95_latency_ms: 1000,
    },
    passed: {
      rate_limit_triggered: (rateLimitedCount / (successCount + rateLimitedCount)) > 0.7,
      latency_threshold: (data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] : 9999) < 1000,
    }
  };
  
  return {
    'stdout': textSummary(summary),
    [`results/rate-limit-test-${Date.now()}.json`]: JSON.stringify(summary, null, 2),
  };
}

function textSummary(summary) {
  return `
Rate Limit Test Summary
=======================
Timestamp: ${summary.timestamp}
Endpoint: ${summary.endpoint}
Total Requests: ${summary.results.total_requests}
Rate Limited: ${summary.results.rate_limited_count} (${summary.results.rate_limit_percentage.toFixed(2)}%)
Successful: ${summary.results.success_count}
Avg Requests Before Limit: ${summary.results.avg_requests_before_limit.toFixed(2)}
P95 Response Time: ${summary.results.p95_response_time_ms.toFixed(2)}ms

Thresholds:
- Rate Limit Triggered (>70%): ${summary.passed.rate_limit_triggered ? '✅ PASS' : '❌ FAIL'}
- P95 Latency (<1000ms): ${summary.passed.latency_threshold ? '✅ PASS' : '❌ FAIL'}
`;
}
