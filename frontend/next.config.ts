import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,

  // API rewrites — all frontend /api/* calls proxy to the Reactive Gateway
  async rewrites() {
    const gatewayUrl = process.env.NEOBANK_GATEWAY_URL || "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${gatewayUrl}/api/:path*`,
      },
    ];
  },

  // OpenTelemetry instrumentation hook for Next.js 16+
  experimental: {
    instrumentationHook: true,
  },
};

export default nextConfig;
