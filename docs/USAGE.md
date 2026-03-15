# NeoBank Usage Guide

Your complete guide to using NeoBank's banking features.

---

## Table of Contents

1. [Onboarding](#onboarding) - Register and get your account
2. [User Roles & Access Control](#user-roles--access-control) - RBAC explained
3. [Credit Scoring](#credit-scoring) - Understand your credit score
4. [Loan Lifecycle](#loan-lifecycle) - Apply, approve, and receive loans
5. [Security](#security) - JWT authentication and API access

---

## Onboarding

### Step 1: Register Your Account

New users can register in seconds. A **$0 savings account** is created automatically upon registration.

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

> **Note:** New users are automatically assigned to the **Head Office** branch and given the `CUSTOMER_RETAIL` role by default.

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

## User Roles & Access Control

NeoBank uses **Role-Based Access Control (RBAC)** to manage permissions across different user types.

### Available Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| `CUSTOMER_RETAIL` | Individual banking customers | Personal accounts, transfers, loans, cards |
| `CUSTOMER_BUSINESS` | Business/corporate customers | Business accounts, commercial loans, multi-user management |
| `TELLER` | Front-line banking staff | Customer lookup, cash transactions, basic operations |
| `RELATIONSHIP_OFFICER` | Customer relationship managers | Customer portfolio, loan recommendations |
| `MANAGER` | Branch/department managers | Loan approvals, staff oversight, exceptions |
| `AUDITOR` | Compliance and audit staff | Audit logs, transaction history, reports |
| `SYSTEM_ADMIN` | System administrators | Full system access |

### Role Assignment

**Default Assignment:**
- New users registering via the public API are assigned `CUSTOMER_RETAIL` by default
- All users are auto-assigned to the **Head Office** branch

**Assigning Roles During Registration:**

For internal user creation (staff accounts), include the role in the registration request:

```bash
# Register a TELLER account
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{
    "username": "teller_jane",
    "email": "jane@neobank.com",
    "password": "securePassword123",
    "role": "TELLER"
  }'

# Register a MANAGER account
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{
    "username": "manager_bob",
    "email": "bob@neobank.com",
    "password": "securePassword123",
    "role": "MANAGER"
  }'
```

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

**Need Help?** Check [Architecture Docs](ARCHITECTURE.md) for technical details or [CONTRIBUTING.md](../CONTRIBUTING.md) for support.
