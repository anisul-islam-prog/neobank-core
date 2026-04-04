// OpenTelemetry instrumentation hook for Next.js 16+
// Called once when the server starts.
export async function register() {
  if (process.env.NEXT_RUNTIME === "nodejs") {
    const { NodeSDK } = await import("@opentelemetry/sdk-node");
    const { OTLPTraceExporter } = await import(
      "@opentelemetry/exporter-trace-otlp-http"
    );
    const { Resource } = await import("@opentelemetry/resources");
    const {
      SemanticResourceAttributes,
    } = await import("@opentelemetry/semantic-conventions");

    const sdk = new NodeSDK({
      resource: new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]: "neobank-frontend",
      }),
      traceExporter: new OTLPTraceExporter({
        url:
          process.env.OTEL_EXPORTER_OTLP_ENDPOINT ||
          "http://otel-collector:4318/v1/traces",
      }),
    });

    sdk.start();

    // Graceful shutdown on process exit
    process.on("SIGTERM", () => {
      sdk
        .shutdown()
        .then(() => console.log("OTel SDK shut down"))
        .catch((err) => console.error("OTel shutdown error", err))
        .finally(() => process.exit(0));
    });
  }
}
