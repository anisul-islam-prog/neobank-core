/**
 * Fraud detection module using AI-powered analysis and rule-based detection.
 * Monitors transfers for suspicious patterns, velocity violations, and blacklist hits.
 * Integrates with Maker-Checker protocol for automatic manual review flagging.
 * 
 * Features:
 * - Velocity Checks: Flag users making more than 3 transfers in 1 minute
 * - Blacklist: Repository of blocked IPs, Account IDs, and User IDs
 * - Threshold Integration: Auto-send suspicious transactions to Manager queue
 * - AI Analysis: Optional AI-powered transaction risk scoring
 * - Observability: Custom Micrometer metrics (bank.fraud.detected.total)
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"transfers", "approvals"}
)
package com.neobank.fraud;
