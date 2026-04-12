# NeoBank — Deployment Guide

> **Production-grade deployment ecosystem for the NeoBank 9-service Spring Boot 3.5.13 cluster.**

---

## Architecture Overview

```
                    ┌─────────────────────────────┐
                    │      Ingress (nginx)         │
                    │   neobank.example.com:443    │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │    neobank-gateway :8080     │  ← 2-3 replicas
                    │  Spring Cloud Gateway        │
                    └──┬──┬──┬──┬──┬──┬──┬──┬─────┘
                       │  │  │  │  │  │  │  │
          ┌────────────┘  │  │  │  │  │  │  └────────────┐
          ▼               ▼  ▼  ▼  ▼  ▼  ▼               ▼
     ┌─────────┐  ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
     │  Auth   │  │Onboarding│ │CoreBanking│ │ Lending  │ │  Cards   │
     │  :8081  │  │  :8082   │ │  :8083   │ │  :8084  │ │  :8085   │
     └─────────┘  └─────────┘ └──────────┘ └──────────┘ └──────────┘
     ┌─────────┐  ┌─────────┐ ┌──────────┐
     │  Fraud  │  │  Batch   │ │Analytics │
     │  :8086  │  │  :8087   │ │  :8088   │
     └─────────┘  └─────────┘ └──────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │     PostgreSQL 17            │
                    │  (external managed DB)       │
                    └─────────────────────────────┘
```

### Service Tiers

| Tier | Services | Replicas (Prod) | Criticality |
|------|----------|:---:|:---:|
| **Gateway** | `neobank-gateway` | 3 | 🔴 Critical — single entry point |
| **Core** | `neobank-auth`, `neobank-core-banking` | 3 each | 🔴 Critical — auth + transactions |
| **Business** | `onboarding`, `lending`, `cards`, `fraud`, `batch`, `analytics` | 1 each | 🟡 Standard — circuit breaker protected |

---

## Directory Structure

```
neobank-core/
├── .github/workflows/
│   ├── ci.yml                    # Original test-only workflow (kept for reference)
│   └── ci-cd.yml                 # ★ Production CI/CD pipeline
├── docker/                       # ★ Per-service multi-stage Dockerfiles
│   ├── gateway/Dockerfile
│   ├── auth/Dockerfile
│   ├── onboarding/Dockerfile
│   ├── core-banking/Dockerfile
│   ├── lending/Dockerfile
│   ├── cards/Dockerfile
│   ├── fraud/Dockerfile
│   ├── batch/Dockerfile
│   └── analytics/Dockerfile
├── k8s/                          # ★ Kubernetes Kustomize manifests
│   ├── base/                     # Base templates (all 9 services)
│   │   ├── kustomization.yaml
│   │   ├── namespace.yaml
│   │   ├── configmap.yaml        # OTel endpoint, gateway routes, Spring profiles
│   │   ├── secret.yaml           # DB creds, JWT secret, OpenAI keys (PLACEHOLDERS)
│   │   ├── deployment-*.yaml     # 9 deployment manifests
│   │   ├── service-*.yaml        # 9 ClusterIP service manifests
│   │   └── ingress.yaml          # nginx Ingress with TLS
│   └── overlays/
│       └── prod/                 # Production overlay
│           └── kustomization.yaml # Image tags, replica scaling, prod profile
├── scripts/
│   ├── deploy-prod.sh            # ★ Zero-downtime deployment script
│   └── verify-deployment.sh      # ★ Smoke test & verification script
└── DEPLOYMENT_GUIDE.md           # ★ This file
```

---

## CI/CD Pipeline (`.github/workflows/ci-cd.yml`)

### Trigger

| Event | Behavior |
|-------|----------|
| `push` to `main` | Full pipeline: Build → Test → Docker Build → Scan → Push → Deploy |
| `pull_request` to `main` | Build + Test only (no deploy) |
| `workflow_dispatch` | Manual trigger with environment selection (production only) |

### Stage 1: Build & Test (Zero-Skip)

```yaml
mvn clean install    # Tests are NEVER skipped
```

- Java 21 (Temurin) with Maven dependency caching
- PostgreSQL 17 service container for integration tests
- **Pipeline fails immediately** if any test fails
- Test results uploaded as artifacts for 7-day retention

### Stage 2: Docker Build (Load Only)

- 9 parallel jobs (one per service) using `docker/build-push-action`
- Images are built with `load: true` — stored in the runner's local Docker daemon, **not pushed to GHCR yet**
- Each service builds from a **multi-stage Dockerfile** (`docker/<service>/Dockerfile`)
- Builder stage: `eclipse-temurin:21-jdk-alpine` + Maven
- Runtime stage: `eclipse-temurin:21-jre-alpine` (non-root user, JRE only)
- Images tagged as `<service-name>:latest` for local scanning
- BuildKit layer caching for fast rebuilds

### Stage 3: Security Scan (Trivy)

- Scans all 9 **local** images for `CRITICAL` and `HIGH` vulnerabilities
- Results uploaded to **GitHub Security tab** as SARIF
- **Blocking** (`exit-code: '1'`) — pipeline fails if critical/high vulnerabilities are found
- Scans the local image tag (e.g., `analytics:latest`) — no registry round-trip, no race condition

### Stage 4: Docker Push (GHCR)

- Only runs **after** Trivy confirms all images are clean
- 9 parallel pushes to GitHub Container Registry
- Images tagged with `${GITHUB_SHA}` and `latest`

### Stage 5: Deploy

- Applies Kustomize production overlay: `kubectl apply -k k8s/overlays/prod`
- Waits for Auth + Core Banking before proceeding
- Rolling update for remaining business modules
- Requires `KUBE_CONFIG` secret (base64-encoded kubeconfig)

---

## Kubernetes Configuration

### Resource Limits (Per Pod)

| Resource | Request | Limit |
|----------|---------|-------|
| Memory | 512Mi | 512Mi (1Gi for Gateway in prod) |
| CPU | 500m | 1000m (1500m for Gateway in prod) |

### Health Probes

All services expose three probe types via Spring Boot Actuator:

| Probe | Endpoint | Initial Delay | Period | Purpose |
|-------|----------|:---:|:---:|---------|
| **Startup** | `/actuator/health` | 30s | 10s | JVM warmup (up to 3 min) |
| **Liveness** | `/actuator/health/liveness` | 90s | 15s | Detect deadlocks |
| **Readiness** | `/actuator/health/readiness` | 60s | 10s | Accept traffic when ready |

### Networking

- **Gateway**: ClusterIP + Ingress (nginx, TLS via cert-manager/Let's Encrypt)
- **All other services**: ClusterIP only (internal, unreachable from outside)
- Gateway routes to downstream services via Kubernetes internal DNS:
  `http://neobank-auth.neobank.svc.cluster.local:8081`

### ConfigMap (`neobank-config`)

Contains shared configuration for all services:
- Spring profile (`prod` / `prod,observability`)
- Database connection URL (internal Kubernetes DNS)
- OpenTelemetry collector endpoint
- Gateway route URIs (internal DNS for all 8 downstream services)
- JWT issuer URI
- JVM options
- Logging levels

### Secrets (`neobank-secrets`)

> ⚠️ **The base secret.yaml contains PLACEHOLDER values.** In production, use one of:

| Method | Description |
|--------|-------------|
| **SealedSecrets** (Bitnami) | Encrypted secrets committed to Git |
| **External Secrets Operator** | Syncs from AWS Secrets Manager, GCP Secret Manager, or Vault |
| **SOPS + Age** | Encrypted YAML files in a private repo |
| **Manual** | `kubectl create secret generic` (not GitOps-friendly) |

Required secrets:
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (256-bit random string)
- `OPENAI_API_KEY`
- `NEOBANK_SECURITY_COOKIE_DOMAIN`

---

## Deployment Flow

### Prerequisites

1. **Kubernetes cluster** (EKS, GKE, AKS, or self-managed) with:
   - nginx Ingress Controller installed
   - cert-manager configured (for TLS)
   - `kubectl` configured with cluster access

2. **GitHub Repository Secrets**:
   ```
   KUBE_CONFIG    = base64-encoded kubeconfig file
   ```

3. **Container Registry** access (GHCR — uses `GITHUB_TOKEN` automatically)

4. **PostgreSQL 17** running externally (or use Cloud SQL, RDS, etc.)

### Automated Deployment (CI/CD)

Push to `main` triggers the full pipeline automatically:

```
git push origin main
  → Build & Test (Java 21, PostgreSQL 17 container)
  → Docker Build (9 images → GHCR)
  → Trivy Security Scan
  → kubectl apply -k k8s/overlays/prod
  → Wait for Auth + Core Banking Ready
  → Rolling update for remaining services
```

### Manual Deployment

```bash
# 1. Ensure you're authenticated to the cluster
kubectl cluster-info

# 2. Preview what would be applied
./scripts/deploy-prod.sh --dry-run

# 3. Deploy to production
./scripts/deploy-prod.sh

# 4. Verify deployment
./scripts/verify-deployment.sh

# 5. Test fallback routes (scale a service to 0 and verify circuit breaker)
./scripts/verify-deployment.sh --scale-to-zero
```

### Script Options

#### `deploy-prod.sh`

| Flag | Description |
|------|-------------|
| `--dry-run` | Render Kustomize manifests without applying |
| `--namespace <ns>` | Override target namespace (default: `neobank`) |
| `--timeout-critical <s>` | Timeout for critical services (default: 300) |
| `--timeout-business <s>` | Timeout per business module (default: 180) |

#### `verify-deployment.sh`

| Flag | Description |
|------|-------------|
| `--quick` | Gateway health check only |
| `--scale-to-zero` | Test circuit breaker fallback by scaling a service to 0 |
| `--namespace <ns>` | Override target namespace (default: `neobank`) |
| `--gateway-url <url>` | Override gateway URL (default: auto-detected) |

---

## Zero-Downtime Strategy

### Rolling Update Configuration

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1        # Create 1 new pod before removing old
    maxUnavailable: 0  # Never drop below current replica count
```

This guarantees:
- **No service interruption** during deployment
- Old pods continue serving traffic until new pods pass readiness probes
- If a new pod fails readiness, the rollout halts and rolls back automatically

### Deployment Order

```
1. ConfigMaps + Secrets (instant)
2. Services (ClusterIP, instant)
3. Auth Service       ← CRITICAL (wait for Ready)
4. Core Banking       ← CRITICAL (wait for Ready)
5. Onboarding         ← Rolling update
6. Lending            ← Rolling update
7. Cards              ← Rolling update
8. Fraud              ← Rolling update
9. Batch              ← Rolling update
10. Analytics         ← Rolling update
11. Gateway           ← Rolling update (last, so it can route to all new services)
```

### Graceful Shutdown

- `terminationGracePeriodSeconds: 60` — pods get 60s to drain connections
- `JAVA_OPTS` includes `-XX:+ExitOnOutOfMemoryError` for clean OOM restarts
- Spring Boot's `@PreDestroy` hooks flush pending transactions

---

## Troubleshooting

### Pod won't start

```bash
# Check pod events
kubectl describe pod -n neobank -l app.kubernetes.io/name=neobank-auth

# Check container logs
kubectl logs -n neobank -l app.kubernetes.io/name=neobank-auth --tail=100

# Check if image pull is the issue
kubectl get pods -n neobank -o jsonpath='{.items[*].status.containerStatuses[*].state}'
```

### Health probe failing

```bash
# Port-forward to the service and test manually
kubectl port-forward -n neobank svc/neobank-auth 8081:8081 &
curl http://localhost:8081/actuator/health
```

### Circuit breaker open

```bash
# Check circuit breaker state via Gateway
curl http://<gateway>/actuator/circuitbreakerevents

# Reset by restarting the affected downstream service
kubectl rollout restart deployment/neobank-<service> -n neobank
```

### Rollback a deployment

```bash
# View rollout history
kubectl rollout history deployment/neobank-auth -n neobank

# Rollback to previous revision
kubectl rollout undo deployment/neobank-auth -n neobank

# Rollback to specific revision
kubectl rollout undo deployment/neobank-auth -n neobank --to-revision=2
```

### Check Trivy scan results

Go to **GitHub Repository → Security → Code scanning alerts** to view vulnerability reports for each service image.

---

## Scaling

### Horizontal Pod Autoscaler (HPA)

For production workloads exceeding baseline capacity, add HPA resources:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: neobank-gateway-hpa
  namespace: neobank
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: neobank-gateway
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

### Database Connection Pooling

When scaling services, ensure PostgreSQL `max_connections` can handle the total:

```
Total connections = (services × replicas) × (connections per service)
                  = (9 × 2) × 20  ≈ 360 connections default
```

Use PgBouncer or Cloud SQL Proxy for connection pooling in high-scale scenarios.

---

## Security Checklist

- [x] Non-root container users (`appuser:appgroup`)
- [x] JRE-only runtime images (no JDK, no Maven in production)
- [x] Seccomp profile: `RuntimeDefault`
- [x] `runAsNonRoot: true` in pod security context
- [x] Secrets in Kubernetes Secrets (migrate to External Secrets for prod)
- [x] TLS via cert-manager / Let's Encrypt
- [x] Trivy vulnerability scanning in CI/CD
- [x] Circuit breakers on all Gateway → downstream routes
- [ ] Network Policies (restrict pod-to-pod traffic to known paths)
- [ ] Pod Security Admission (enforce `restricted` policy on `neobank` namespace)
- [ ] Image signing / Cosign verification (prevent supply chain attacks)
