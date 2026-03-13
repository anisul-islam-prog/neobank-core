package com.neobank.loans.internal;

import com.neobank.loans.AmortizationEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for generating loan amortization schedules.
 */
@Component
class AmortizationSchedule {

    /**
     * Generate a full amortization schedule for a loan.
     *
     * @param principal the loan amount
     * @param annualInterestRate the annual interest rate (as decimal)
     * @param termMonths the loan term in months
     * @return list of amortization entries
     */
    List<AmortizationEntry> generateSchedule(BigDecimal principal, BigDecimal annualInterestRate, int termMonths) {
        List<AmortizationEntry> schedule = new ArrayList<>(termMonths);

        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(12), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyRate, termMonths);
        BigDecimal balance = principal;

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interestPortion = balance.multiply(monthlyRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal principalPortion = monthlyPayment.subtract(interestPortion);

            // Final payment adjustment
            if (i == termMonths) {
                principalPortion = balance;
                monthlyPayment = principalPortion.add(interestPortion);
            }

            balance = balance.subtract(principalPortion);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            schedule.add(new AmortizationEntry(
                    i,
                    monthlyPayment,
                    principalPortion.setScale(2, BigDecimal.ROUND_HALF_UP),
                    interestPortion,
                    balance
            ));
        }

        return schedule;
    }

    /**
     * Calculate the monthly payment amount.
     *
     * @param principal the loan amount
     * @param monthlyRate the monthly interest rate
     * @param termMonths the loan term in months
     * @return the monthly payment
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = monthlyRate.multiply(onePlusR.pow(termMonths));
        BigDecimal denominator = onePlusR.pow(termMonths).subtract(BigDecimal.ONE);

        return principal.multiply(numerator).divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate total interest payable over the loan term.
     *
     * @param schedule the amortization schedule
     * @return the total interest amount
     */
    BigDecimal calculateTotalInterest(List<AmortizationEntry> schedule) {
        return schedule.stream()
                .map(AmortizationEntry::interestPortion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get the remaining balance after a specific number of payments.
     *
     * @param schedule the amortization schedule
     * @param paymentNumber the payment number to check
     * @return the remaining balance, or null if paymentNumber is out of range
     */
    BigDecimal getRemainingBalance(List<AmortizationEntry> schedule, int paymentNumber) {
        if (paymentNumber < 0 || paymentNumber > schedule.size()) {
            return null;
        }
        if (paymentNumber == 0) {
            // Before any payments, return the first entry's balance + principal portion
            return schedule.isEmpty() ? BigDecimal.ZERO :
                    schedule.get(0).remainingBalance().add(schedule.get(0).principalPortion());
        }
        return schedule.get(paymentNumber - 1).remainingBalance();
    }
}
