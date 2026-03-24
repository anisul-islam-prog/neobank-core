# NeoBank Chaos Engineering & Failure Scenarios

## Overview

This document documents failure scenarios and expected system behavior when components fail. Use this guide for chaos engineering tests, incident response, and system resilience validation.

---

## Failure Scenarios Matrix

| Scenario | Impact | Mitigation | Recovery Time | Severity |
|----------|--------|------------|---------------|----------|
| Auth Database Offline | Login failures, new users blocked | Circuit breaker opens, cached tokens valid | 30s auto | Critical |
| Analytics Module Down | No BI data, transfers succeed | Events queued locally | Immediate | Low |
| Transfer Service Slow | Payment delays | Retry with backoff, bulkhead isolation | 5-10s | High |
| Rate Limit Exceeded | 429 errors for user | Exponential backoff on client | 1 minute | Medium |
| Thread Pool Exhaustion | Non-critical ops blocked | Bulkhead rejects low-priority | Immediate | Medium |

---

## Scenario 1: Auth Database Goes Offline

### What Happens

**Immediate Impact (0-5 seconds):**
- New login attempts fail with 500 error
- JWT token validation continues (tokens cached in memory)
- Existing sessions remain active
- Circuit breaker starts counting failures

**Circuit Breaker Activation (5-30 seconds):**
- After 5 failures in 10-second window, circuit opens
- Auth circuit breaker state: `CLOSED` → `OPEN`
- Fallback activates:
  - Login endpoint returns 503 "Service Temporarily Unavailable"
  - Token validation uses cached public keys
  - Existing user sessions unaffected

**Recovery (30+ seconds):**
- After 30s wait, circuit moves to `HALF_OPEN`
- Next successful login attempt closes circuit
- Normal operation resumes
- Metrics show failure spike

### Expected Behavior

```
┌─────────────────────────────────────────────────────────────┐
│                    Auth Database Failure                     │
├─────────────────────────────────────────────────────────────┤
│ Time    │ State      │ User Impact          │ System Action │
├─────────────────────────────────────────────────────────────┤
│ T+0s    │ DB Down    │ Login fails          │ Retry x3      │
│ T+5s    │ Failures=5 │ Login fails          │ Circuit OPEN  │
│ T+5-35s │ OPEN       │ 503 on login         │ Fallback      │
│ T+35s   │ HALF_OPEN  │ Test request         │ Probe DB      │
│ T+36s   │ CLOSED     │ Login succeeds       │ Normal ops    │
└─────────────────────────────────────────────────────────────┘
```

### How to Test

```bash
# 1. Simulate auth database failure
docker compose exec neobank-auth psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'neobank';"

# 2. Attempt login (should fail initially)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"wrong"}'

# Expected: 503 after 5 attempts

# 3. Check circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers/auth

# Expected: state=OPEN

# 4. Restore database
docker compose restart neobank-auth

# 5. Wait for recovery (30s)
# 6. Verify circuit closed
curl http://localhost:8080/actuator/circuitbreakers/auth

# Expected: state=CLOSED
```

### Monitoring Alerts

- **Alert:** `resilience4j_circuitbreaker_state{name="auth"} == 2` (OPEN)
- **Runbook:** Check auth database health, restart if needed
- **Escalation:** If not recovered in 2 minutes, page on-call

---

## Scenario 2: Analytics Module Failure During Transfer

### What Happens

**Normal Flow:**
```
User → Transfer Request → Core Banking → Analytics (async)
                              ↓
                        Transfer Complete
```

**Failure Flow:**
```
User → Transfer Request → Core Banking → Analytics (DOWN)
                              ↓
                        Transfer Complete ✓
                              ↓
                        Event Queued Locally
```

**Expected Behavior:**
- Transfer succeeds (analytics is non-critical)
- Analytics event queued in `AnalyticsFallbackService`
- Queue size logged (warning at 80%)
- When analytics recovers, events replayed asynchronously

### How to Test

```bash
# 1. Kill analytics module
docker compose stop neobank-analytics

# 2. Execute transfer
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"fromId":"...","toId":"...","amount":100}'

# Expected: 200 OK (transfer succeeds)

# 3. Check queued events
curl http://localhost:8080/actuator/metrics/analytics.events.queued

# Expected: events.queued > 0

# 4. Restart analytics
docker compose start neobank-analytics

# 5. Trigger replay (automatic or manual)
# Events should be sent to analytics

# 6. Verify queue cleared
curl http://localhost:8080/actuator/metrics/analytics.events.queued

# Expected: events.queued = 0
```

### Monitoring

- **Metric:** `analytics_events_queued_total`
- **Alert:** Queue size > 8,000 (80% of 10,000)
- **Dashboard:** Show queued events over time

---

## Scenario 3: Transfer Service Under Heavy Load

### What Happens

**Load Pattern:**
- 100 concurrent transfer requests/second
- Thread pool saturation
- Response times increase

**Bulkhead Protection:**
```
┌─────────────────────────────────────────────────────────────┐
│                    Bulkhead Isolation                        │
├─────────────────────────────────────────────────────────────┤
│ Thread Pool          │ Max Concurrent │ Queue Capacity      │
├─────────────────────────────────────────────────────────────┤
│ critical-executor    │ 50             │ 100                 │
│ non-critical-executor│ 20             │ 50                  │
└─────────────────────────────────────────────────────────────┘
```

**Expected Behavior:**
- Critical transfers (payments) get priority
- Non-critical operations (BI reports) rejected
- Heavy BI report cannot starve transfer operations

### How to Test

```bash
# 1. Generate heavy BI report load (non-critical)
for i in {1..30}; do
  curl http://localhost:8080/api/admin/reports/transactions \
    -H "Authorization: Bearer <admin-token>" &
done

# 2. Simultaneously send transfer requests (critical)
for i in {1..50}; do
  curl -X POST http://localhost:8080/api/transfers \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <token>" \
    -d '{"fromId":"...","toId":"...","amount":10}' &
done

# Expected:
# - Transfers succeed (critical path)
# - Some BI reports rejected with 503 (non-critical)

# 3. Check bulkhead metrics
curl http://localhost:8080/actuator/bulkheads

# Expected:
# - critical bulkhead: concurrent_calls < 50
# - non-critical bulkhead: some rejected calls
```

### Monitoring

- **Metric:** `resilience4j_bulkhead_concurrent_calls`
- **Alert:** Non-critical bulkhead rejection rate > 50%
- **Dashboard:** Show concurrent calls by bulkhead

---

## Scenario 4: Rate Limit Exceeded (Bot Attack)

### What Happens

**Attack Pattern:**
- Bot sends 100 registration requests/minute
- Rate limiter activates
- Subsequent requests rejected with 429

**Rate Limits:**
| Endpoint | Limit | Window |
|----------|-------|--------|
| `/api/onboarding/register` | 5 requests | 1 minute |
| Retail user API | 100 requests | 1 minute |
| Staff user API | 500 requests | 1 minute |
| Unauthenticated (IP) | 60 requests | 1 minute |

### How to Test

```bash
# 1. Simulate bot attack on registration
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/onboarding/register \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"bot$i\",\"email\":\"bot$i@test.com\",\"password\":\"Test123!\"}" &
done

# Expected:
# - First 5 requests: 200 OK or 400 (validation)
# - Requests 6-10: 429 Too Many Requests

# 2. Check rate limit headers
curl -v http://localhost:8080/api/onboarding/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"Test123!"}'

# Expected headers:
# X-RateLimit-Limit: 5
# X-RateLimit-Remaining: 0
# X-RateLimit-Reset: 60

# 3. Wait for window reset (60 seconds)
# 4. Verify requests succeed again
```

### Monitoring

- **Metric:** `rate_limit_exceeded_total`
- **Alert:** > 100 rate limit rejections/minute from single IP
- **Dashboard:** Show rate limit rejections by endpoint

---

## Scenario 5: Network Latency Injection

### What Happens

**Latency Impact:**
- Inter-module calls take 5-10 seconds
- Timeout errors increase
- Circuit breakers may open

**Retry Behavior:**
```
Attempt 1: T+0s (fails with timeout)
Attempt 2: T+1s (fails with timeout) - exponential backoff
Attempt 3: T+3s (succeeds)
```

### How to Test

```bash
# 1. Inject network latency (using tc on Linux)
docker compose exec neobank-core-banking tc qdisc add dev eth0 root netem delay 100ms

# 2. Execute transfer
time curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"fromId":"...","toId":"...","amount":100}'

# Expected: Transfer succeeds with retry (1-3 seconds total)

# 3. Check retry metrics
curl http://localhost:8080/actuator/metrics/resilience4j.retry.calls

# 4. Remove latency
docker compose exec neobank-core-banking tc qdisc del dev eth0 root netem
```

---

## Scenario 6: Cascading Failure Prevention

### What Happens

**Without Circuit Breakers (Bad):**
```
Auth Down → Onboarding Fails → Transfers Fail → Cards Fail
              (5s timeout)      (5s timeout)    (5s timeout)
Total cascade time: 15+ seconds
System completely unavailable
```

**With Circuit Breakers (Good):**
```
Auth Down → Onboarding Fails (circuit opens after 5 failures)
Transfers → Use cached auth (succeeds)
Cards → Use cached auth (succeeds)
Total impact: Limited to auth-dependent operations only
```

### How to Test

```bash
# 1. Kill auth module
docker compose stop neobank-auth

# 2. Send mixed requests
curl http://localhost:8080/api/transfers &  # Should succeed (cached)
curl http://localhost:8080/api/auth/login & # Should fail (503)
curl http://localhost:8080/api/cards &      # Should succeed (cached)

# Expected:
# - Auth-dependent ops fail fast (circuit open)
# - Other ops succeed (cached/isolated)

# 3. Verify isolation
curl http://localhost:8080/actuator/circuitbreakers

# Expected: Only 'auth' circuit breaker is OPEN
```

---

## Recovery Procedures

### General Recovery Steps

1. **Identify the failing component**
   - Check `/actuator/health`
   - Review circuit breaker states
   - Check logs for exceptions

2. **Assess impact**
   - Which endpoints affected?
   - User-facing or internal?
   - Data loss risk?

3. **Decide: Wait vs. Intervene**
   - Auto-recovery expected? (most cases: yes)
   - Manual intervention needed? (database corruption, etc.)

4. **Recovery actions**
   - Restart container
   - Rollback recent deployment
   - Scale up resources

5. **Post-recovery verification**
   - Circuit breakers closed
   - Metrics normalized
   - No error spike

### Emergency Contacts

| Role | Contact | Escalation Time |
|------|---------|-----------------|
| On-Call Engineer | oncall@neobank.com | Immediate |
| Platform Lead | platform-lead@neobank.com | 15 minutes |
| CTO | cto@neobank.com | 1 hour |

---

## Chaos Testing Schedule

| Week | Scenario | Owner | Success Criteria |
|------|----------|-------|------------------|
| 1 | Auth DB failure | Platform Team | Circuit opens in <10s |
| 2 | Analytics failure | Core Team | Transfers succeed, events queued |
| 3 | Rate limit test | Security Team | 429 after limit exceeded |
| 4 | Full cascade | All Teams | Isolation prevents cascade |

---

## Metrics & Dashboards

### Key Resilience Metrics

```promql
# Circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
resilience4j_circuitbreaker_state{name="auth"}

# Failure rate percentage
resilience4j_circuitbreaker_failure_rate{name="transfer"}

# Bulkhead utilization
resilience4j_bulkhead_concurrent_calls{name="critical"}

# Rate limit remaining tokens
resilience4j_ratelimiter_available_tokens{name="module-calls"}

# Retry attempts
resilience4j_retry_calls{state="failed"}
```

### Grafana Dashboard Panels

1. **Circuit Breaker States** - Gauge for each circuit
2. **Failure Rate Over Time** - Line chart by module
3. **Retry Success Rate** - Percentage of successful retries
4. **Rate Limit Rejections** - Count by endpoint
5. **Bulkhead Utilization** - Heatmap of concurrent calls

---

**Document Version:** 1.0
**Created:** 2026-03-24
**Review Schedule:** Monthly chaos testing
**Owner:** Platform Engineering Team
