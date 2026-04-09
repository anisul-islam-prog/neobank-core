# NeoBank Frontend

Next.js + Tailwind CSS dashboards for NeoBank Core.

## Overview

The project includes **three separate Next.js frontend applications**:

| App | Port | Purpose |
|-----|------|---------|
| Retail App | 3000 | Customer banking dashboard |
| Staff Portal | 3001 | KYC & approval workflows |
| Admin Console | 3002 | BI dashboards & system monitoring |

## Getting Started

### Prerequisites

- **Node.js 24.14.1+** (required for Next.js 16)
- **pnpm 10+** (package manager)
- Backend services running (ports 8080-8088)

### Installation

```bash
# Install pnpm globally (if not already installed)
npm install -g pnpm

# Retail App
cd apps/retail-app
pnpm install

# Staff Portal
cd apps/staff-portal
pnpm install

# Admin Console
cd apps/admin-console
pnpm install
```

### Development

```bash
# Retail App
cd apps/retail-app
pnpm dev

# Staff Portal
cd apps/staff-portal
pnpm dev

# Admin Console
cd apps/admin-console
pnpm dev
```

Open in your browser:
- Retail App: http://localhost:3000
- Staff Portal: http://localhost:3001
- Admin Console: http://localhost:3002

## Features

### Retail App
- **Login/Register**: JWT-based authentication
- **Dashboard**: View accounts and balances
- **Transfers**: Real-time fund transfers
- **Cards**: View and manage cards
- **Transaction History**: Complete transaction log

### Staff Portal
- **KYC Queue**: User approval workflow
- **Maker-Checker**: Transfer approval system
- **Credit Management**: Score adjustments
- **Loan Processing**: Application review

### Admin Console
- **BI Dashboard**: Transaction analytics
- **User Management**: System-wide user control
- **System Health**: Service monitoring
- **Configuration**: System parameters

## API Configuration

All frontends proxy API requests to the backend Gateway at `http://localhost:8080` via `next.config.ts`:

```typescript
// next.config.ts
export default defineConfig({
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ]
  },
})
```

## Tech Stack

- **Next.js 16** - React framework with App Router
- **React 19** - UI library
- **TypeScript 5.8+** - Type safety
- **Tailwind CSS** - Utility-first CSS framework
- **Recharts** - Chart visualizations
- **js-cookie** - Cookie management for JWT storage
- **OpenTelemetry NodeSDK** - Distributed tracing

## Observability

Frontend traces are sent to the OTel Collector via `src/instrumentation.ts`:

```typescript
// src/instrumentation.ts
import { registerOTel } from '@vercel/otel';

export function register() {
  registerOTel({
    serviceName: 'retail-app', // or staff-portal, admin-console
    traceExporter: {
      endpoint: 'http://otel-collector:4318/v1/traces',
    },
  });
}
```

## Security Notes

- JWT tokens are stored in HttpOnly cookies
- All API requests include Authorization header
- Card details are masked by default, revealed on-demand
- CSRF protection via backend cookies

## Build for Production

```bash
# All apps
cd apps/retail-app && pnpm build
cd apps/staff-portal && pnpm build
cd apps/admin-console && pnpm build

# Start production servers
pnpm start
```

## Testing

```bash
# Run tests
pnpm test

# Run E2E tests (requires backend running)
pnpm test:e2e
```

---

*Last Updated: April 9, 2026*
*Next.js 16 · React 19 · TypeScript 5.8 · Node.js 24 · pnpm 10*
