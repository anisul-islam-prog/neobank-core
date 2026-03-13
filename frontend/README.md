# NeoBank Frontend

Next.js + Tailwind CSS dashboard for NeoBank Core.

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
# Start the development server
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## Features

- **Login/Register**: JWT-based authentication
- **Dashboard**: View accounts and balances
- **Cards**: View cards with masked numbers
- **Reveal**: Secure endpoint to reveal full card details

## API Configuration

The frontend proxies API requests to the backend at `http://localhost:8080` via `next.config.js`.

## Tech Stack

- **Next.js 14** - React framework with App Router
- **Tailwind CSS** - Utility-first CSS framework
- **js-cookie** - Cookie management for JWT storage
- **TypeScript** - Type safety

## Security Notes

- JWT tokens are stored in cookies (1 day expiry)
- All API requests include Authorization header
- Card details are masked by default, revealed on-demand
