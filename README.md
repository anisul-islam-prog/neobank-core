# NeoBank

> Next-generation digital banking built for the future.

[![Java CI](https://github.com/anisul-islam-prog/neobank-core/actions/workflows/ci.yml/badge.svg)](https://github.com/anisul-islam-prog/neobank-core/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## What is NeoBank?

NeoBank is a modern, cloud-native banking platform featuring:

- 🏦 **Instant Account Opening** - Get started in seconds with automatic savings accounts
- 💳 **Smart Card Management** - Virtual and physical cards with real-time controls
- 💰 **AI-Powered Loans** - Risk-based lending with instant decisions
- 🔒 **Bank-Grade Security** - JWT authentication, AES-256 encryption, fraud detection
- 📊 **Beautiful Dashboard** - Intuitive Next.js interface for all your banking needs

Built with Java 25, Spring Boot 4, and Next.js 14.

---

## Getting Started

### Prerequisites

| Requirement | Version | Purpose | Download |
|-------------|---------|---------|----------|
| **Docker** | 24+ | Database + AI services | [docker.com](https://www.docker.com/get-started) |
| **Java** | 25+ | Backend development | [adoptium.net](https://adoptium.net) |
| **Node.js** | 18+ | Frontend development | [nodejs.org](https://nodejs.org) |
| **Maven** | 3.9+ | Backend build | [maven.apache.org](https://maven.apache.org) |

---

## Profile Overview

| Profile | Infrastructure | Backend | Frontend | AI | Use Case |
|---------|---------------|---------|----------|-----|----------|
| **dev** | Docker (PostgreSQL + Ollama) | Local (Maven) | Local (npm) | Local (Ollama) | Daily development |
| **test** | Docker (all) | Docker | Docker | Local (Ollama) | Integration testing |
| **prod** | Docker (all) | Docker | Docker | Cloud (OpenAI) | Production deployment |
| **demo** | Docker (all) + seed data | Docker | Docker | Local (Ollama) | Demos, presentations |

---

## Dev Profile (Development)

Run **PostgreSQL + Ollama in Docker**, **backend + frontend locally** for fast iteration.

### Step 1: Start Infrastructure (Docker)

```bash
cd neobank-core

# Start PostgreSQL and Ollama only
docker-compose --profile dev up -d

# Wait for healthy status (~30 seconds)
docker-compose ps
```

### Step 2: Start Backend (Terminal 1)

```bash
cd neobank-core
mvn spring-boot:run
```

### Step 3: Start Frontend (Terminal 2)

```bash
cd neobank-core/frontend
npm install  # first time only
npm run dev
```

### Access

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| PostgreSQL | localhost:5432 |
| Ollama | localhost:11434 |

### Stop

```bash
docker-compose --profile dev down  # Stop Docker
# Ctrl+C for backend and frontend terminals
```

---

## Test Profile (Integration Testing)

Run **everything in Docker** with **local AI (Ollama)** for realistic testing.

### Start Test Stack

```bash
cd neobank-core

# Start all services in Docker (uses Ollama for fraud detection)
docker-compose --profile test up -d --build

# Wait ~60 seconds for all services
docker-compose logs -f
```

### Access

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### Stop

```bash
docker-compose --profile test down
```

---

## Prod Profile (Production)

Run **everything in Docker** with **OpenAI cloud AI** for production deployment.

### Start Production Stack

```bash
cd neobank-core

# Set OpenAI API key
export OPENAI_API_KEY=sk-your-api-key-here

# Start all services (uses OpenAI instead of Ollama)
docker-compose --profile prod up -d --build

# Backend runs with: SPRING_PROFILES_ACTIVE=openai
```

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | Yes | OpenAI API key for fraud detection |
| `NEXT_PUBLIC_APP_ENV` | No | Frontend environment (default: production) |

### Access

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |

### Stop

```bash
docker-compose --profile prod down
```

---

## Demo Profile (Presentations)

Run **everything in Docker with seeded data** - fake users, transactions, fraud alerts.

### Start Demo Stack

```bash
cd neobank-core

# Enable seed data
export NEOBANK_SEED_DATA=true

# Start all services with mock data
docker-compose --profile demo up -d --build

# Wait ~90 seconds (includes data seeding)
docker-compose logs -f
```

### Pre-Seed Data Includes

- **Users**: All role types (CUSTOMER_RETAIL, TELLER, MANAGER, SYSTEM_ADMIN, etc.)
- **Accounts**: Multiple accounts with varying balances
- **Transactions**: Fake transaction history
- **Loans**: Sample loan applications (approved, pending, rejected)
- **Cards**: Virtual and physical cards with different statuses
- **Fraud Alerts**: Generated fraud detection alerts
- **Tokens**: Pre-generated Swagger access tokens

### Demo Login Credentials

```
Retail User:
  username: customer_john
  password: demo123!

Teller:
  username: teller_jane
  password: demo123!

Manager:
  username: manager_bob
  password: demo123!

Admin:
  username: admin_alice
  password: demo123!
```

> ⚠️ **Warning:** Demo data is for presentation only. Do not use in production.

### Stop

```bash
docker-compose --profile demo down
```

---

## Troubleshooting

### First Build Takes Too Long

**Expected behavior:**
- First build: 5-10 minutes (Maven + npm build inside Docker)
- Ollama download: ~5GB llama3.2 model on first run
- Subsequent builds: < 30 seconds (cached layers)

**Check build progress:**
```bash
docker-compose --profile dev up -d --build --progress=plain
docker-compose logs -f
```

### Port 3000 Already in Use (OpenWebUI, etc.)

```bash
# Find what's using port 3000
lsof -ti:3000

# Kill the process (macOS/Linux)
lsof -ti:3000 | xargs kill -9

# Or change frontend port in docker-compose.yml:
# services:
#   frontend:
#     ports:
#       - "3005:3000"  # Use port 3005 instead
```

### Backend Not Starting

```bash
# Check if PostgreSQL is healthy
docker-compose ps
# Should show: neobank-postgres - healthy

# View backend logs
docker-compose logs backend

# Restart backend only
docker-compose restart backend
```

### Ollama Not Downloading Model

```bash
# Check Ollama status
docker-compose logs ollama

# Pull model manually
docker-compose exec ollama ollama pull llama3.2

# Verify model is ready
docker-compose exec ollama ollama run llama3.2 'hello'
```

### Frontend Shows "Failed to Fetch"

```bash
# Frontend can't reach backend - check network
docker-compose logs frontend

# Verify backend is accessible from frontend container
docker-compose exec frontend wget -q -O- http://backend:8080/actuator/health

# Should return: {"status":"UP"}
```

### New User Cannot Login

```bash
# New registrations have PENDING status
# Must be approved by MANAGER or RELATIONSHIP_OFFICER

# Generate SYSTEM_ADMIN token (you need to create one first)
# Then approve via Swagger UI: PUT /api/auth/users/{userId}/approve

# Or via API:
curl -X PUT http://localhost:8080/api/auth/users/{userId}/approve \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN"
```

### Swagger UI Access Denied

```bash
# Generate access token (requires SYSTEM_ADMIN role)
curl -X POST http://localhost:8080/api/auth/admin/docs/tokens \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SYSTEM_ADMIN_TOKEN" \
  -d '{"description": "Dev Access", "durationHours": 24}'

# Use returned accessUrl or:
# http://localhost:8080/swagger-ui.html?access_token=TOKEN
```

### Reset Everything

```bash
# Stop all containers
docker-compose --profile dev down

# Remove volumes (deletes all data!)
docker-compose --profile dev down -v

# Rebuild from scratch
docker-compose --profile dev up -d --build --force-recreate
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [📖 Usage Guide](docs/USAGE.md) | Banking manual: accounts, loans, cards, and security |
| [🏗️ Architecture](docs/ARCHITECTURE.md) | Technical deep-dive, diagrams, and roadmap |
| [🤝 Contributing](CONTRIBUTING.md) | How to contribute to NeoBank |

---

## Features at a Glance

### Accounts
- Automatic savings account on registration
- Real-time balance updates
- Transaction history with categorization

### Loans
- Instant credit scoring (0-100)
- Risk-based interest rates
- Automated amortization schedules

### Cards
- Virtual card issuance
- Spend controls and limits
- Merchant category blocking
- Secure card details reveal

### Security
- JWT-based authentication
- BCrypt password hashing
- AES-256-GCM card encryption
- AI-powered fraud detection

### Security & Compliance
- **Verified Onboarding**: All new users require staff approval before accessing banking features
- **Role-Based Access Control (RBAC)**: 8 distinct roles from CUSTOMER_RETAIL to SYSTEM_ADMIN
- **User Status Management**: PENDING, ACTIVE, SUSPENDED states for account lifecycle control
- **Forced Password Reset**: Staff accounts must change password on first login
- **Segregation of Duties**: MANAGER approves customers, SYSTEM_ADMIN approves staff
- **Audit Trail**: All approval actions logged with user and timestamp

---

## Development

For detailed setup instructions, architecture details, and advanced configuration, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

### Run Locally (Full Stack)

```bash
# Terminal 1 - Backend
docker-compose --profile local up -d

# Terminal 2 - Frontend
cd frontend && npm install && npm run dev
```

### Run Tests

```bash
mvn clean test
```

---

## Tech Stack

**Backend:** Java 25 · Spring Boot 4 · Spring Modulith · PostgreSQL  
**Frontend:** Next.js 14 · React · Tailwind CSS · TypeScript  
**AI:** Spring AI · Ollama (local) · OpenAI (cloud)  
**Security:** JWT · BCrypt · AES-256-GCM

---

## License

NeoBank is open-source under the [MIT License](LICENSE).

---

**Built with ❤️ using Java 25 and Spring Boot 4**
