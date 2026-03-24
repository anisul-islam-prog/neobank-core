# NeoBank

> Next-generation digital banking platform with modular microservices architecture.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 🚀 Quick Start

```bash
# Start infrastructure
docker-compose --profile dev up -d

# Run backend
mvn spring-boot:run

# Run frontend (retail)
cd apps/retail-app && npm install && npm run dev
```

**Access:**
- Retail App: http://localhost:3000
- Staff Portal: http://localhost:3001  
- Admin Console: http://localhost:3002
- API: http://localhost:8080

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [📖 Operational Manual](docs/USAGE.md) | Complete user guide for customers, staff, and admins |
| [🏗️ Architecture](docs/ARCHITECTURE.md) | Technical deep-dive and system design |
| [🔄 Migration Plan](MIGRATION_PLAN.md) | Microservice migration roadmap |
| [🤝 Contributing](CONTRIBUTING.md) | How to contribute to NeoBank |

---

## 🏛️ Platform Overview

### For Customers
- 🏦 Instant savings account on registration
- 💸 Real-time fund transfers
- 💳 Virtual and physical card management
- 📱 Intuitive dashboard at http://localhost:3000

### For Staff (Tellers, Managers, ROs)
- ✅ KYC user approval workflow
- 🔒 Maker-Checker protocol for high-value transfers (>$5,000)
- 📊 Credit score management and adjustment
- 💰 Loan application processing
- Access at http://localhost:3001

### For Administrators
- 📈 BI Dashboard with transaction analytics
- 🎯 Risk distribution and credit score heatmaps
- 📋 KYC funnel visualization
- System monitoring and health checks
- Access at http://localhost:3002

---

## 🏗️ Architecture

NeoBank uses a **Modular Monolith** architecture with Spring Modulith:

```
neobank-parent/
├── neobank-gateway/        # API Gateway (port 8081)
├── neobank-auth/           # Authentication (schema_auth)
├── neobank-onboarding/     # KYC & User Status (schema_onboarding)
├── neobank-core-banking/   # Accounts, Transfers (schema_core)
├── neobank-lending/        # Loans (schema_loans)
├── neobank-cards/          # Cards (schema_cards)
├── neobank-batch/          # EOD Reconciliation (schema_batch)
└── neobank-analytics/      # BI & CQRS (schema_analytics)
```

### Key Features
- **Schema Isolation**: Each module has its own database schema
- **Event-Driven**: Spring Modulith events for cross-module communication
- **Maker-Checker**: Dual authorization for high-value operations
- **CQRS**: Read-optimized BI tables for analytics

---

## 🔒 Security

- JWT authentication with audience claims (retail/staff/admin)
- BCrypt password hashing
- AES-256-GCM card encryption
- Role-Based Access Control (8 roles)
- Maker-Checker protocol for sensitive operations

---

## 🧪 Testing

```bash
# Run all tests
./test-all.sh

# Run specific suites
./test-all.sh --skip-backend   # Skip backend tests
./test-all.sh --skip-frontend  # Skip frontend tests
./test-all.sh --skip-e2e       # Skip E2E tests
```

---

## 📋 Demo Credentials

| Portal | Username | Password |
|--------|----------|----------|
| Retail | customer_john | demo123! |
| Staff (Manager) | manager_bob | demo123! |
| Admin | admin_alice | demo123! |

---

## 🛠️ Tech Stack

**Backend:** Java 25 · Spring Boot 4 · Spring Modulith · PostgreSQL  
**Frontend:** Next.js 14 · React · TypeScript · Tailwind CSS · Recharts  
**AI:** Spring AI · Ollama (local) · OpenAI (cloud)  
**Testing:** JUnit 5 · Testcontainers · Vitest · Playwright

---

## 📄 License

NeoBank is open-source under the [MIT License](LICENSE).

---

**Built with ❤️ using Java 25 and Spring Boot 4**
