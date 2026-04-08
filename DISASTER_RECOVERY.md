# NeoBank AI Disaster Recovery Guide

> **Version:** 2026.1 | **Spring Boot:** 3.5.13 LTS | **Last Updated:** April 2026
> 
> This runbook covers incident response for AI-dependent services: **Lending (8084)** and **Fraud (8086)**.

---

## Table of Contents

1. [AI Service Architecture](#ai-service-architecture)
2. [Scenario A: OpenAI API Outage](#scenario-a-openai-api-outage)
3. [Scenario B: Token Rate Limiting (429)](#scenario-b-token-rate-limiting-429)
4. [Scenario C: Model Hallucination / Safety Trigger](#scenario-c-model-hallucination--safety-trigger)
5. [Kill Switch: Disable AI Globally](#kill-switch-disable-ai-globally)
6. [Incident Response Runbook](#incident-response-runbook)

---

## AI Service Architecture

| Service | Port | AI Model | Purpose | Fallback |
|---------|------|----------|---------|----------|
| **Lending** | 8084 | GPT-4o-mini (OpenAI) | Creditworthiness scoring, risk profile generation | Rule-based deterministic scoring |
| **Fraud** | 8086 | GPT-4o-mini (OpenAI) | Transaction pattern analysis, anomaly detection | Heuristic-based rule engine |

Both services use `spring-ai-openai` dependency with configurable base URLs, model parameters, and API keys via environment variables.

---

## Scenario A: OpenAI API Outage

### Impact
- Loan applications cannot receive AI-generated risk assessments
- Fraud analysis cannot evaluate transaction patterns via AI models
- Services throw `OpenAiApiException` or timeout after 30s

### Automatic Failover

#### Lending Service (8084)
When OpenAI is unreachable, the `CreditScoreService` detects the failure and switches to **Rule-Based Deterministic Approval**:

```java
// Pseudocode - CreditScoreService.java
public RiskProfile evaluateCredit(LoanApplication app) {
    try {
        return aiModel.evaluate(app); // Spring AI OpenAI call
    } catch (OpenAiApiException e) {
        log.warn("OpenAI unavailable - falling back to rule-based scoring");
        return deterministicScoring(app);
    }
}

private RiskProfile deterministicScoring(LoanApplication app) {
    // Hard-coded credit score thresholds
    if (app.creditScore() >= 750 && app.income() >= 50000) {
        return new RiskProfile("LOW", 0.12);  // 12% interest
    } else if (app.creditScore() >= 650) {
        return new RiskProfile("MEDIUM", 0.18); // 18% interest
    } else {
        return new RiskProfile("HIGH", 0.25);   // 25% interest or reject
    }
}
```

| Credit Score Range | Income Threshold | Risk Level | Interest Rate | Decision |
|-------------------|-----------------|------------|---------------|----------|
| 750-850 | ≥ $50,000 | LOW | 12% | ✅ Auto-approve |
| 650-749 | ≥ $35,000 | MEDIUM | 18% | ⚠️ Auto-approve with conditions |
| 580-649 | ≥ $25,000 | HIGH | 25% | 🔍 Manual review |
| < 580 | Any | REJECT | N/A | ❌ Auto-reject |

#### Fraud Service (8086)
Falls back to **Heuristic Rule Engine**:

```java
// Pseudocode - FraudAnalysisService.java
public FraudResult analyzeTransaction(Transaction tx) {
    try {
        return aiModel.analyze(tx); // Spring AI OpenAI call
    } catch (OpenAiApiException e) {
        log.warn("OpenAI unavailable - using heuristic rules");
        return heuristicAnalysis(tx);
    }
}

private FraudResult heuristicAnalysis(Transaction tx) {
    int riskScore = 0;
    
    // Rule 1: Velocity check - >5 transactions in 1 hour
    if (tx.countLastHour() > 5) riskScore += 30;
    
    // Rule 2: Amount threshold - >$10,000
    if (tx.amount() > 10000) riskScore += 25;
    
    // Rule 3: Off-hours transaction (2am-5am)
    if (tx.isOffHours()) riskScore += 20;
    
    // Rule 4: New payee
    if (tx.isNewPayee()) riskScore += 15;
    
    // Rule 5: Geographic anomaly
    if (tx.isGeographicAnomaly()) riskScore += 30;
    
    if (riskScore >= 70) return FraudResult.flagged(riskScore);
    if (riskScore >= 40) return FraudResult.suspicious(riskScore);
    return FraudResult.clear(riskScore);
}
```

### Detection
```bash
# Check Lending service health
curl -s http://localhost:8084/actuator/health | jq '.components.ai'

# Check Fraud service health
curl -s http://localhost:8086/actuator/health | jq '.components.ai'

# Expected when degraded:
# {"status":"DEGRADED","details":{"openai":"UNAVAILABLE","fallback":"ACTIVE"}}
```

### Recovery
1. Verify OpenAI status: `https://status.openai.com`
2. When restored, services automatically resume AI model calls on next request
3. No restart required — connection is re-established on demand

---

## Scenario B: Token Rate Limiting (429)

### Impact
OpenAI returns `429 Too Many Requests` when token quota or RPM limit is exceeded.

### Automatic Handling via Resilience4j

#### Retry Configuration
```yaml
# application.properties (Lending & Fraud)
resilience4j.retry.instances.openai-api.max-attempts=3
resilience4j.retry.instances.openai-api.wait-duration=2s
resilience4j.retry.instances.openai-api.enable-exponential-backoff=true
resilience4j.retry.instances.openai-api.exponential-backoff-multiplier=2
resilience4j.retry.instances.openai-api.max-wait-duration=15s
resilience4j.retry.instances.openai-api.retry-exceptions=\
  io.github.resilience4j.ratelimiter.RequestNotPermitted,\
  org.springframework.web.client.HttpClientErrorException$TooManyRequests
```

#### Retry Flow
```
Request → AI Model → 429 Response → Retry (2s delay)
  → 429 Response → Retry (4s delay, exponential)
  → 429 Response → Retry (8s delay)
  → 429 Response → Exhaust retries → FALLBACK
```

#### Fallback: Manual Review Queue
When all retries are exhausted:

| Service | Fallback Action |
|---------|----------------|
| **Lending** | Application moves to `PENDING_REVIEW` status. Staff portal notifies loan officers via `LoanManualReviewEvent`. |
| **Fraud** | Transaction is flagged `MANUAL_REVIEW_REQUIRED`. Fraud analysts review via the admin console. |

```java
// Event published when AI is rate-limited
@ApplicationEvent
public class LoanManualReviewEvent {
    public UUID loanId;
    public String reason; // "AI_RATE_LIMITED"
    public Instant timestamp;
    public int retryAttempts;
}
```

### Monitoring
```bash
# Check retry metrics via Prometheus
curl -s http://localhost:8084/actuator/prometheus | grep resilience4j_retry

# Key metrics:
# resilience4j_retry_calls_total{name="openai-api",result="failed"}  # Should be 0
# resilience4j_retry_calls_total{name="openai-api",result="retry"}   # > 0 indicates rate limiting
```

### Escalation Thresholds

| Metric | Warning | Critical | Action |
|--------|---------|----------|--------|
| Retry failure rate | > 5% over 5 min | > 20% over 5 min | Page on-call engineer |
| 429 responses/min | > 10 | > 50 | Activate manual review queue |
| Queue depth (manual reviews) | > 20 | > 100 | Alert management, consider increasing OpenAI tier |

---

## Scenario C: Model Hallucination / Safety Trigger

### Impact
AI returns anomalous scores (e.g., credit score of 1200, fraud probability of 200%), or confidence below acceptable threshold.

### Safety Guardrails

#### Confidence Score Threshold: 70%

Both services validate AI responses before accepting them:

```java
// Lending - CreditScoreService.java
public RiskProfile evaluateWithGuardrails(LoanApplication app) {
    AiResponse<RiskProfile> response = aiModel.evaluate(app);
    
    if (response.confidence() < 0.70) {
        log.warn("AI confidence below threshold: {} for loan {}",
            response.confidence(), app.id());
        return deterministicScoring(app); // Fall back to rules
    }
    
    // Validate score ranges
    if (response.score() < 300 || response.score() > 850) {
        log.error("AI returned out-of-range credit score: {}", response.score());
        return deterministicScoring(app);
    }
    
    return response.result();
}
```

```java
// Fraud - FraudAnalysisService.java
public FraudResult analyzeWithGuardrails(Transaction tx) {
    AiResponse<FraudResult> response = aiModel.analyze(tx);
    
    if (response.confidence() < 0.70) {
        log.warn("AI confidence below threshold: {} for tx {}",
            response.confidence(), tx.id());
        return FraudResult.manualReview("AI_LOW_CONFIDENCE");
    }
    
    // Validate probability range [0.0, 1.0]
    if (response.fraudProbability() < 0.0 || response.fraudProbability() > 1.0) {
        log.error("AI returned invalid fraud probability: {}", response.fraudProbability());
        return FraudResult.manualReview("AI_INVALID_OUTPUT");
    }
    
    return response.result();
}
```

### Human-in-the-Loop Trigger Conditions

| Trigger Condition | Confidence | Action | Notification |
|------------------|------------|--------|-------------|
| Credit score confidence < 70% | < 0.70 | Fall back to rule-based scoring | Log warning |
| Fraud analysis confidence < 70% | < 0.70 | Flag for manual review | Alert fraud analyst |
| Out-of-range credit score (<300 or >850) | N/A | Fall back to rule-based | Error log + alert |
| Out-of-range fraud probability (<0 or >1) | N/A | Flag for manual review | Error log + alert |
| Response time > 15s | N/A | Timeout → manual review | Circuit breaker trigger |

### Admin Dashboard Alerts
When confidence is low:
1. **Staff Portal** → "Loan Review Queue" shows application with `AI_UNCERTAIN` badge
2. **Admin Console** → "Fraud Review Queue" shows transaction with `MANUAL_REVIEW_REQUIRED`
3. **Audit Trail** → Each fallback decision logged with reason code and timestamp

---

## Kill Switch: Disable AI Globally

Disable all AI features across the platform **without redeploying**.

### Method 1: Runtime via Spring Boot Admin / Actuator

```bash
# Disable AI in Lending (8084)
curl -X POST http://localhost:8084/actuator/env \
  -H "Content-Type: application/json" \
  -d '{"name": "neobank.ai.enabled", "value": "false"}'

# Disable AI in Fraud (8086)
curl -X POST http://localhost:8086/actuator/env \
  -H "Content-Type: application/json" \
  -d '{"name": "neobank.ai.enabled", "value": "false"}'
```

### Method 2: Environment Variable (Container-level)

```bash
# In docker-compose.yml or Kubernetes deployment
environment:
  NEOBANK_AI_ENABLED: "false"
```

### Method 3: Configuration Property

```properties
# application.properties (or via Config Server / Spring Cloud Config)
neobank.ai.enabled=false
```

### Effect of Kill Switch

When `neobank.ai.enabled=false`:

| Service | Behavior |
|---------|----------|
| **Lending (8084)** | All loan applications use deterministic rule-based scoring. No AI calls attempted. |
| **Fraud (8086)** | All transactions evaluated via heuristic rule engine. No AI calls attempted. |
| **Gateway (8080)** | Circuit breakers remain closed. No degradation since services self-handle fallback. |
| **Observability** | Metrics show `ai_status=DISABLED` in Prometheus dashboards. |

### Verification
```bash
# Verify AI is disabled
curl -s http://localhost:8084/actuator/env/neobank.ai.enabled
# Expected: {"property":{"value":"false","origin":"..."}}

# Check service behavior
curl -s -X POST http://localhost:8084/api/loans/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"accountId":"...", "amount":5000, "purpose":"personal"}'

# Response should include "scoringMethod": "RULE_BASED"
```

---

## Incident Response Runbook

### Severity Classification

| Severity | Condition | Response Time | Escalation |
|----------|-----------|---------------|------------|
| **SEV-1 (Critical)** | Both Lending + Fraud AI unavailable > 15 min | Immediate | Engineering Lead + VP |
| **SEV-2 (High)** | One AI service unavailable > 10 min | 15 min | On-call engineer |
| **SEV-3 (Medium)** | Rate limiting causing > 20% retry rate | 30 min | On-call engineer |
| **SEV-4 (Low)** | Confidence scores trending below 70% | 4 hours | Team review next sprint |

### Response Procedure

```
1. DETECT
   ├── Alert from Prometheus (5xx spike, retry rate, confidence drop)
   ├── Dashboard observation (Grafana AI status panel)
   └── User report (loan officer / fraud analyst)

2. ASSESS
   ├── Check OpenAI status: https://status.openai.com
   ├── Check service health: curl http://<service>:<port>/actuator/health
   ├── Check logs: kubectl logs -f <pod> | grep -E "OpenAI|AI|429"
   └── Determine: Is this OpenAI outage, rate limiting, or hallucination?

3. CONTAIN
   ├── If OpenAI outage → Services auto-fallback (verify fallback is ACTIVE)
   ├── If rate limiting → Activate kill switch if queue depth > 100
   ├── If hallucination → Activate kill switch, audit recent AI decisions
   └── Notify affected teams (staff portal, admin console)

4. RESOLVE
   ├── Wait for OpenAI restoration (if outage)
   ├── Verify services resume AI calls automatically
   ├── Audit any decisions made during fallback period
   └── Update incident timeline

5. POST-MORTEM
   ├── Document root cause
   ├── Update runbook if new failure mode discovered
   ├── Review retry/backoff configuration
   └── Consider additional safeguards (caching, model redundancy)
```

### Useful Commands

```bash
# Check all service health at once
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088; do
  status=$(curl -s http://localhost:$port/actuator/health 2>/dev/null | jq -r '.status')
  printf "Port %-5s: %s\n" "$port" "${status:-DOWN}"
done

# Check AI-specific health
curl -s http://localhost:8084/actuator/health | jq '.components.ai'
curl -s http://localhost:8086/actuator/health | jq '.components.ai'

# View recent AI-related errors
kubectl logs -l app=neobank-lending --since=1h | grep -E "ERROR|WARN" | grep -i "ai\|openai\|429"
kubectl logs -l app=neobank-fraud --since=1h | grep -E "ERROR|WARN" | grep -i "ai\|openai\|429"

# Check Resilience4j circuit breaker states
curl -s http://localhost:8084/actuator/circuitbreakerevents | jq '.circuitBreakerEvents[-5:]'
curl -s http://localhost:8086/actuator/circuitbreakerevents | jq '.circuitBreakerEvents[-5:]'

# Prometheus queries (via Grafana or direct)
# AI error rate: sum(rate(http_requests_total{status=~"5..",service=~"lending|fraud"}[5m]))
# Retry rate: sum(rate(resilience4j_retry_calls_total{result="retry"}[5m]))
# Queue depth: manual_review_queue_depth
```

---

*This document is maintained by the SRE team. Last reviewed: April 2026.*
*Next review: May 2026 or after any SEV-1 incident.*
