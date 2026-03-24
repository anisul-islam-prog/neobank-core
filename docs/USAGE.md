# NeoBank Operational Manual

Your complete guide to operating NeoBank's banking platform.

---

## Table of Contents

### For Customers
1. [Onboarding](#onboarding) - Register and get your account
2. [User Status & Approval Workflow](#user-status--approval-workflow) - Account lifecycle
3. [Credit Scoring](#credit-scoring) - Understand your credit score
4. [Loan Lifecycle](#loan-lifecycle) - Apply, approve, and receive loans

### For Staff (Tellers, Managers, ROs)
5. [How to Approve a User](#how-to-approve-a-user) - KYC approval workflow
6. [How to Clear the Maker-Checker Queue](#how-to-clear-the-maker-checker-queue) - High-value transfer approvals
7. [Credit Management](#credit-management) - Adjust credit scores for customers

### For Administrators
8. [How to Read the BI Dashboard](#how-to-read-the-bi-dashboard) - Analytics and reporting
9. [Security](#security) - JWT authentication and API access
10. [System Monitoring](#system-monitoring) - Health checks and alerts

---

## Onboarding

### Step 1: Register Your Account

New users can register in seconds. A **$0 savings account** is created automatically upon registration.

**Important:** Public registration creates an account with `PENDING` status and `ROLE_GUEST`. 
Users must be approved by a MANAGER or RELATIONSHIP_OFFICER before they can login and access banking features.

**Via Dashboard:**
1. Open http://localhost:3000
2. Click "Don't have an account? Sign up"
3. Fill in:
   - **Username**: Your unique identifier
   - **Email**: For notifications
   - **Password**: Minimum 8 characters
4. Click "Sign Up"

**Via API:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "success": true,
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "User registered successfully"
}
```

> **Note:** New users are assigned to the **Head Office** branch with `PENDING` status and `ROLE_GUEST`. 
> Approval is required before accessing banking features.

### Step 2: Login

After registration, login to receive your JWT token.

**Via Dashboard:**
1. Enter your username and password
2. Click "Sign In"
3. You'll be redirected to your dashboard

**Via API:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

> ⚠️ **Save your token!** You'll need it for all authenticated requests.

### Step 3: Your Automatic Account

Upon registration, NeoBank automatically creates:

| Account Type | Initial Balance | Purpose |
|--------------|-----------------|---------|
| **Savings Account** | $0.00 | Your primary account for deposits and transfers |

**View your account:**

```bash
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## User Status & Approval Workflow

NeoBank implements a **verified onboarding process** to ensure security and compliance.

### User Status Values

| Status | Description | Can Login? | Can Apply for Loans/Cards? |
|--------|-------------|------------|---------------------------|
| `PENDING` | Awaiting staff approval | ❌ No | ❌ No |
| `ACTIVE` | Fully verified and active | ✅ Yes | ✅ Yes |
| `SUSPENDED` | Temporarily disabled (fraud/compliance review) | ❌ No | ❌ No |

### Approval Workflow

**For Public Registrations (CUSTOMER_RETAIL):**

1. User registers via `/api/auth/register` → Status: `PENDING`, Role: `ROLE_GUEST`
2. MANAGER or RELATIONSHIP_OFFICER approves via `PUT /api/auth/users/{id}/approve`
3. User status changes to `ACTIVE`, role changes to `CUSTOMER_RETAIL`
4. User can now login and access banking features

```bash
# Approve a pending user (MANAGER or RELATIONSHIP_OFFICER only)
curl -X PUT http://localhost:8080/api/auth/users/{user-id}/approve \
  -H "Authorization: Bearer MANAGER_TOKEN"
```

**Response:**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "success": true,
  "message": "User approved and activated"
}
```

### Login Enforcement

When a user attempts to login:

| Status | Response Code | Message |
|--------|---------------|---------|
| `PENDING` | 403 Forbidden | "Account pending approval. Please contact support." |
| `SUSPENDED` | 403 Forbidden | "Account suspended. Please contact support." |
| `ACTIVE` (mustChangePassword=true) | 200 OK + flag | Response includes `mustChangePassword: true` |
| `ACTIVE` (normal) | 200 OK | Standard login response |

**Force Password Reset Flow:**

When `mustChangePassword` is `true` in the login response, the frontend must:
1. Redirect user to password reset page
2. User sets new password
3. Call `POST /api/auth/reset-password` to clear the flag

---

## Staff-Led Onboarding

For creating staff accounts (TELLER, MANAGER, etc.) or pre-approved customer accounts.

### Staff Onboarding Endpoint

**Access Control:**
- `SYSTEM_ADMIN`: Can create any role
- `MANAGER`: Can create TELLER, CUSTOMER_RETAIL, CUSTOMER_BUSINESS
- `RELATIONSHIP_OFFICER`: Can create TELLER, CUSTOMER_RETAIL, CUSTOMER_BUSINESS

```bash
# Onboard a new TELLER (MANAGER or RO can do this)
curl -X POST http://localhost:8080/api/auth/onboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "username": "teller_jane",
    "email": "jane@neobank.com",
    "password": "TempPass123!",
    "role": "TELLER",
    "branchId": "branch-uuid-optional"
  }'
```

**Response:**
```json
{
  "userId": "staff-uuid-123",
  "success": true,
  "message": "User registered successfully"
}
```

> **Note:** Staff users are created with `mustChangePassword = true` to force password reset on first login.

### Staff Approval Workflow

For staff accounts with sensitive roles (MANAGER, AUDITOR), SYSTEM_ADMIN approval is required:

```bash
# Approve staff user (SYSTEM_ADMIN only)
curl -X PUT http://localhost:8080/api/auth/staff/{user-id}/approve \
  -H "Authorization: Bearer SYSTEM_ADMIN_TOKEN"
```

### User Status Management

MANAGER and SYSTEM_ADMIN can update user status:

```bash
# Suspend a user (for fraud/compliance review)
curl -X PATCH "http://localhost:8080/api/auth/users/{user-id}/status?status=SUSPENDED" \
  -H "Authorization: Bearer MANAGER_TOKEN"

# Reactivate a user
curl -X PATCH "http://localhost:8080/api/auth/users/{user-id}/status?status=ACTIVE" \
  -H "Authorization: Bearer MANAGER_TOKEN"
```

---

## User Roles & Access Control

NeoBank uses **Role-Based Access Control (RBAC)** to manage permissions across different user types.

### Available Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| `ROLE_GUEST` | Unverified users awaiting approval | None - must be approved |
| `CUSTOMER_RETAIL` | Individual banking customers | Personal accounts, transfers, loans, cards |
| `CUSTOMER_BUSINESS` | Business/corporate customers | Business accounts, commercial loans, multi-user management |
| `TELLER` | Front-line banking staff | Customer lookup, cash transactions, basic operations |
| `RELATIONSHIP_OFFICER` | Customer relationship managers | Customer portfolio, loan recommendations |
| `MANAGER` | Branch/department managers | Loan approvals, staff oversight, exceptions |
| `AUDITOR` | Compliance and audit staff | Audit logs, transaction history, reports |
| `SYSTEM_ADMIN` | System administrators | Full system access |

### Role Assignment

**Default Assignment:**
- New users registering via the public API are assigned `ROLE_GUEST` with `PENDING` status
- Users must be approved by MANAGER or RELATIONSHIP_OFFICER to become `CUSTOMER_RETAIL`
- All users are auto-assigned to the **Head Office** branch

**Staff Onboarding:**

For internal user creation (staff accounts), use the `/api/auth/onboard` endpoint:

```bash
# Onboard a TELLER account (MANAGER or RO can do this)
curl -X POST http://localhost:8080/api/auth/onboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "username": "teller_jane",
    "email": "jane@neobank.com",
    "password": "TempPass123!",
    "role": "TELLER"
  }'
```

> **Note:** The old registration method with role parameter is deprecated. Use `/api/auth/onboard` for staff accounts.

### Protected Endpoints by Role

| Endpoint | Required Role(s) | Description |
|----------|------------------|-------------|
| `POST /api/loans/**/approve` | `MANAGER`, `SYSTEM_ADMIN` | Approve pending loans |
| `GET /api/audit/**` | `AUDITOR` | Access audit logs and reports |
| `GET /api/accounts/search/**` | `TELLER+` | Search accounts (non-owned) |
| `GET /api/loans/**` | Authenticated | View personal loans |
| `POST /api/transfers/**` | Authenticated | Make transfers |

> **Note:** `TELLER+` means TELLER or any higher role (RELATIONSHIP_OFFICER, MANAGER, AUDITOR, SYSTEM_ADMIN)

### Testing Role-Based Access

```bash
# As a MANAGER - approve a loan (allowed)
curl -X POST http://localhost:8080/api/loans/loan-123/approve \
  -H "Authorization: Bearer MANAGER_TOKEN"

# As CUSTOMER_RETAIL - approve a loan (403 Forbidden)
curl -X POST http://localhost:8080/api/loans/loan-123/approve \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
# Response: 403 Forbidden

# As AUDITOR - access audit logs (allowed)
curl http://localhost:8080/api/audit/logs \
  -H "Authorization: Bearer AUDITOR_TOKEN"

# As CUSTOMER_RETAIL - access audit logs (403 Forbidden)
curl http://localhost:8080/api/audit/logs \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
# Response: 403 Forbidden
```

---

## Credit Scoring

NeoBank calculates a **credit score (0-100)** for every user based on their financial profile. Higher scores indicate higher risk.

### How Credit Scores Are Calculated

The credit score is derived from four key factors:

| Factor | Weight | Impact |
|--------|--------|--------|
| **Credit History** | 40% | Payment history and existing credit |
| **Debt-to-Income Ratio** | 30% | Monthly debt vs. monthly income |
| **Employment Stability** | 15% | Years at current job |
| **Income Level** | 15% | Annual income adequacy |

### Score Ranges

| Score Range | Risk Level | Loan Approval Chance |
|-------------|------------|---------------------|
| **0-24** | Low Risk | ✅ Excellent |
| **25-49** | Medium Risk | ✅ Good |
| **50-74** | High Risk | ⚠️ Fair |
| **75-100** | Very High Risk | ❌ Poor |

### Check Your Credit Score

**Via API:**

```bash
curl http://localhost:8080/api/loans/credit-score \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "creditScore": 35,
  "riskLevel": "MEDIUM",
  "factors": {
    "creditHistory": "Good standing",
    "debtToIncome": 0.25,
    "employmentYears": 3,
    "annualIncome": 65000
  }
}
```

### Improve Your Score

- ✅ **Pay bills on time** - Consistent payments lower risk
- ✅ **Reduce debt** - Lower DTI ratio improves score
- ✅ **Stable employment** - Longer tenure = better score
- ✅ **Increase income** - Higher income reduces risk

---

## Loan Lifecycle

### Step 1: Check Eligibility

Before applying, check your credit score to understand your approval chances.

```bash
# Check your credit score first
curl http://localhost:8080/api/loans/credit-score \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Step 2: Apply for a Loan

**Submit loan application:**

```bash
curl -X POST http://localhost:8080/api/loans/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "amount": 10000,
    "termMonths": 24,
    "purpose": "HOME_IMPROVEMENT",
    "annualIncome": 75000,
    "employmentYears": 5,
    "monthlyDebt": 1500
  }'
```

**Response:**
```json
{
  "applicationId": "loan-123-abc",
  "status": "APPROVED",
  "approvedAmount": 10000,
  "interestRate": 5.5,
  "monthlyPayment": 438.71,
  "totalInterest": 529.04
}
```

### Step 3: Review Loan Terms

After approval, review your loan details:

| Field | Description |
|-------|-------------|
| **Approved Amount** | The principal you'll receive |
| **Interest Rate** | Annual percentage rate (APR) |
| **Term** | Repayment period in months |
| **Monthly Payment** | Fixed monthly installment |
| **Total Interest** | Total interest over the loan term |

### Step 4: Accept and Disburse

**Disburse the loan to your account:**

```bash
curl -X POST http://localhost:8080/api/loans/disburse/loan-123-abc \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "disbursementId": "disb-456-def",
  "loanId": "loan-123-abc",
  "accountId": "acct-789-ghi",
  "amount": 10000,
  "disbursedAt": "2026-03-15T10:30:00Z",
  "status": "COMPLETED"
}
```

### Step 5: Repayment

Loans are automatically repaid via monthly deductions from your linked account.

**View loan status:**

```bash
curl http://localhost:8080/api/loans/loan-123-abc \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## How to Approve a User

Managers and Relationship Officers can approve pending user registrations through the Staff Portal.

### Via Staff Portal

1. Login to http://localhost:3001 with your MANAGER or RELATIONSHIP_OFFICER credentials
2. Navigate to the **KYC Approvals** tab
3. Find the pending user in the list
4. Click **Approve** to activate their account

### Via API

```bash
curl -X PUT http://localhost:8080/api/onboarding/users/{userId}/approve \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN"
```

**Response:**
```json
{
  "userId": "user-uuid",
  "status": "ACTIVE",
  "approvedBy": "manager-uuid",
  "approvedAt": "2024-01-15T10:30:00Z"
}
```

---

## How to Clear the Maker-Checker Queue

The Maker-Checker protocol requires Manager approval for high-value transfers (>$5,000).

### Understanding the Queue

- **Maker**: A TELLER initiates a high-value transfer
- **Checker**: A MANAGER must approve before the transfer executes
- Transfers remain in `PENDING_AUTH` status until approved or rejected

### Via Staff Portal

1. Login to http://localhost:3001 with MANAGER credentials
2. Navigate to the **Maker-Checker Queue** tab (or /dashboard/approvals)
3. Review pending authorizations:
   - View transfer amount and reason
   - Check initiator details
4. Click **Review** on a pending authorization
5. Add optional review notes
6. Click **Approve** to execute the transfer or **Reject** to cancel

### Via API

**Get pending authorizations:**
```bash
curl http://localhost:8080/api/approvals/pending \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN"
```

**Approve an authorization:**
```bash
curl -X POST http://localhost:8080/api/approvals/{authorizationId}/approve \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN" \
  -H "X-User-Id: manager-uuid" \
  -H "X-User-Role: MANAGER" \
  -d '{"notes": "Verified with customer"}'
```

**Reject an authorization:**
```bash
curl -X POST http://localhost:8080/api/approvals/{authorizationId}/reject \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN" \
  -H "X-User-Id: manager-uuid" \
  -H "X-User-Role: MANAGER" \
  -d '{"notes": "Insufficient documentation"}'
```

---

## Credit Management

Relationship Officers can adjust customer credit scores with documented reasons.

### Via Staff Portal

1. Login to http://localhost:3001 with RELATIONSHIP_OFFICER credentials
2. Navigate to **Credit Management** (/dashboard/credit)
3. Search for a customer by username
4. Click **Manage** to view their credit profile
5. Review:
   - Current credit score and risk level
   - DTI ratio, employment history, income
   - Recent transaction history
6. Enter new score (300-850) and adjustment reason
7. Click **Update Credit Score**

### Credit Score Ranges

| Range | Risk Level | Color |
|-------|------------|-------|
| 700-850 | Low Risk | 🟢 Green |
| 600-699 | Medium Risk | 🟡 Yellow |
| 300-599 | High Risk | 🔴 Red |

---

## How to Read the BI Dashboard

The BI Dashboard (admin-console) provides real-time analytics from the `schema_analytics` database.

### Accessing the Dashboard

1. Login to http://localhost:3002 with SYSTEM_ADMIN credentials
2. Navigate to **BI Dashboard** (/dashboard/bi)

### Dashboard Widgets

#### Summary Cards
- **Total Transactions**: Sum of all transfer volumes
- **Active Users**: Count of ACTIVE status users
- **Pending Approvals**: Maker-Checker queue count
- **Avg Credit Score**: Platform-wide average

#### Transaction Volume Trend (Line Chart)
- Shows daily/monthly money flow
- Purple line: Transaction volume ($)
- Green line: Transaction count

#### Risk Distribution (Pie Chart)
- 🟢 Low Risk (700+): Healthy credit profiles
- 🟡 Medium Risk (600-699): Average credit
- 🔴 High Risk (<600): Requires monitoring

#### KYC Funnel (Bar Chart)
- Registered → Pending Review → Approved → Rejected
- Shows onboarding conversion rates

### Data Source

Data is populated from the Analytics module which listens to:
- `MoneyTransferredEvent` - For transaction history
- `UserCreatedEvent` - For user registration metrics
- Loan events - For credit analytics

---

## Security

### JWT Authentication

All authenticated API endpoints require a JWT token in the request header.

#### Using JWT Tokens

**1. Get your token:**

```bash
# Login to receive a token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "securePassword123"}'
```

**2. Include token in requests:**

```bash
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Token Format

```
Authorization: Bearer <your-jwt-token>
```

| Component | Description |
|-----------|-------------|
| `Authorization` | Standard HTTP header name |
| `Bearer` | Authentication scheme |
| `<token>` | Your JWT token from login |

#### Token Expiry

- **Default validity**: 24 hours
- **After expiry**: Login again to get a new token
- **Security**: Tokens are single-use and cannot be reused after logout

### Protected Endpoints

The following endpoints require authentication:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/loans/**` | All | Loan applications and management |
| `/api/cards/**` | All | Card operations |
| `/api/transfers/**` | All | Fund transfers |
| `/api/accounts/**` | GET/PUT | Account details (POST is public) |

### Public Endpoints

These endpoints do NOT require authentication:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | User registration |
| `/api/auth/login` | POST | User login |
| `/api/accounts` | POST | Create new account |
| `/swagger-ui.html` | GET | API documentation |

### Security Best Practices

1. **Never share your token** - Treat it like a password
2. **Use HTTPS** - Always connect via secure channels
3. **Store securely** - Use environment variables or secure vaults
4. **Rotate regularly** - Login periodically for fresh tokens
5. **Logout when done** - Invalidate tokens after use

### Testing with Swagger UI

The easiest way to test authenticated endpoints:

1. Open http://localhost:8080/swagger-ui.html
2. Click the **Authorize** button (lock icon)
3. Enter your JWT token (without "Bearer " prefix)
4. Click **Authorize**
5. All endpoints now include your token automatically

---

## Quick Reference

### Common API Calls

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "email": "user@example.com", "password": "pass123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass123"}'

# Check credit score
curl http://localhost:8080/api/loans/credit-score \
  -H "Authorization: Bearer YOUR_TOKEN"

# Apply for loan
curl -X POST http://localhost:8080/api/loans/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount": 5000, "termMonths": 12, "purpose": "PERSONAL", "annualIncome": 50000, "employmentYears": 2, "monthlyDebt": 1000}'

# View accounts
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Error Codes

| Code | Meaning | Solution |
|------|---------|----------|
| `401` | Unauthorized | Check your JWT token |
| `400` | Bad Request | Verify request body format |
| `404` | Not Found | Check resource ID |
| `500` | Server Error | Contact support |

---

## Observability

NeoBank includes a comprehensive observability stack for monitoring, tracing, and debugging.

### Starting the Observability Stack

```bash
# Start all observability services
docker-compose --profile observability up -d

# View status
docker-compose --profile observability ps
```

### Accessing Observability Tools

| Tool | URL | Credentials | Purpose |
|------|-----|-------------|---------|
| **Grafana** | http://localhost:3000 | admin / admin123 | Dashboards & alerts |
| **Prometheus** | http://localhost:9090 | - | Metrics queries |
| **Tempo** | http://localhost:3200 | - | Trace search |
| **Loki** | http://localhost:3100 | - | Log queries |

### Grafana Dashboards

After starting Grafana, access the pre-configured **NeoBank Operations Dashboard**:

1. Navigate to http://localhost:3000
2. Login with `admin` / `admin123`
3. Go to Dashboards → NeoBank → NeoBank Operations Dashboard

**Dashboard Panels:**
- **System Health**: CPU, Memory, Threads per module
- **Business Pulse**: Total liquidity, transactions, accounts, failures
- **Request Latency**: Response time percentiles (p95, p99)

### Searching for a Specific Trace

When a user reports a bug, use the trace ID from error logs or response headers:

**Step 1: Get the Trace ID**
- Check response header `X-Trace-Id` from failed API calls
- Or find it in application logs: `traceId=abc123...`

**Step 2: Search in Grafana**
1. Go to **Explore** in Grafana
2. Select **Tempo** datasource
3. Paste the trace ID in the search box
4. Click **Run query**

**Step 3: Analyze the Trace**
- View the complete request flow across modules
- Identify slow spans or errors
- Click on spans to see detailed attributes

**Step 4: Correlate with Logs**
1. In the trace view, click **Show logs**
2. Grafana automatically queries Loki for logs matching the trace ID
3. Review logs from all services involved in the trace

### Querying Metrics in Prometheus

**Total Transactions:**
```promql
bank_transactions_total
```

**Transaction Rate (per second):**
```promql
rate(bank_transactions_total[5m])
```

**Failed Transfers:**
```promql
bank_transfers_failed
```

**Total Vault Liquidity:**
```promql
bank_vault_total_liquidity
```

**Request Latency (p99):**
```promql
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))
```

### Custom Business Metrics

NeoBank exposes these custom metrics via `/actuator/prometheus`:

| Metric | Type | Description |
|--------|------|-------------|
| `bank_transactions_total` | Counter | Cumulative transaction count |
| `bank_accounts_created` | Counter | Total accounts created |
| `bank_transfers_failed` | Counter | Failed transfer attempts |
| `bank_vault_total_liquidity` | Gauge | Sum of all account balances |
| `bank_transfer_duration_seconds` | Timer | Transfer operation latency |

### Setting Up Alerts

**Example: High Failure Rate Alert**

1. In Grafana, go to **Alerting** → **New Alert Rule**
2. Set query: `rate(bank_transfers_failed[5m]) > 0.1`
3. Set evaluation: Every 1 minute, for 5 minutes
4. Configure notification channel (email, Slack, etc.)

### Log Aggregation with Loki

**Query recent error logs:**
```logql
{app="neobank"} |= "ERROR" | line_format "{{.message}}"
```

**Query logs for specific trace:**
```logql
{app="neobank"} |= "traceId=abc123"
```

### Troubleshooting

**Grafana shows no data:**
- Verify Prometheus is scraping targets: http://localhost:9090/targets
- Check backend actuator endpoint: http://localhost:8080/actuator/prometheus

**Traces not appearing:**
- Verify backend has `management.tracing.sampling.probability=1.0`
- Check Tempo is receiving data: http://localhost:3200/status

**Logs not appearing:**
- Verify Promtail is running: `docker-compose ps promtail`
- Check Promtail configuration for correct log paths

---

**Need Help?** Check [Architecture Docs](ARCHITECTURE.md) for technical details or [CONTRIBUTING.md](../CONTRIBUTING.md) for support.
