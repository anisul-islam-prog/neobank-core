package com.neobank.loans;

import java.math.BigDecimal;

/**
 * A single entry in a loan amortization schedule.
 *
 * @param paymentNumber the payment sequence number (1-based)
 * @param paymentAmount the total payment amount
 * @param principalPortion the portion applied to principal
 * @param interestPortion the portion applied to interest
 * @param remainingBalance the outstanding balance after this payment
 */
public record AmortizationEntry(
        int paymentNumber,
        BigDecimal paymentAmount,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal remainingBalance
) {
}
