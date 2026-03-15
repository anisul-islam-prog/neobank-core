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

## Getting Started (2 Minutes)

### Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| Docker | 24+ | [docker.com](https://www.docker.com/get-started) |

That's it! Docker includes everything needed to run NeoBank.

### Quick Start

**1. Start the application:**

```bash
# Clone and enter the project
git clone https://github.com/anisul-islam-prog/neobank-core.git
cd neobank-core

# Start all services (PostgreSQL + Backend)
docker-compose --profile local up -d

# Wait ~30 seconds for services to initialize
docker-compose logs -f
```

**2. Open your browser:**

- **Dashboard:** http://localhost:3000
- **API Docs:** http://localhost:8080/swagger-ui.html (requires access token, see below)

**3. Access API Documentation:**

API documentation is restricted for security. SYSTEM_ADMIN must generate an access token:

```bash
# Generate documentation access token (SYSTEM_ADMIN only)
curl -X POST http://localhost:8080/api/auth/admin/docs/tokens \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SYSTEM_ADMIN_TOKEN" \
  -d '{"description": "Dev Access", "durationHours": 24}'
```

Use the returned `accessUrl` to access Swagger UI, or append `?access_token=TOKEN` to the URL.

**3. Create your first account:**

1. Click "Sign Up" on the login page
2. Enter your details (username, email, password)
3. Your $0 savings account is created automatically
4. **Note:** New accounts require staff approval before login (see [Usage Guide](docs/USAGE.md))
5. Once approved, login and start exploring!

> **For Testing:** Use the Swagger UI to approve accounts: `PUT /api/auth/users/{id}/approve`

### Stopping NeoBank

```bash
# Stop all services
docker-compose --profile local down
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
