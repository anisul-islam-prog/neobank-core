package com.neobank.fraud;

import com.neobank.core.transfers.MoneyTransferredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-powered fraud detection service.
 * Analyzes transfer transactions using AI to detect suspicious patterns.
 * Uses autoconfigured ChatClient.Builder to support hybrid AI strategy (OpenAI or Ollama).
 * Token usage is automatically tracked via Spring AI's built-in Micrometer observation.
 */
@Service
@ConditionalOnClass(ChatClient.class)
@ConditionalOnProperty(name = "spring.ai.enabled", havingValue = "true", matchIfMissing = true)
public class FraudListener {

    private static final Logger log = LoggerFactory.getLogger(FraudListener.class);
    private static final Pattern RISK_SCORE_PATTERN = Pattern.compile("(\\d+)");

    private final ChatClient chatClient;

    public FraudListener(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("fraudAnalysisExecutor")
    void onTransferCompleted(MoneyTransferredEvent event) {
        analyzeTransaction(event);
    }

    private void analyzeTransaction(MoneyTransferredEvent event) {
        String prompt = buildFraudAnalysisPrompt(event);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            int riskScore = extractRiskScore(response);

            // Spring AI automatically tracks token usage via Micrometer observation
            // Metrics: gen_ai.client.token.usage with input/output token counts
            // Tags include: gen_ai.operation.name, gen_ai.system, spring.ai.kind

            if (riskScore > 80) {
                log.warn("[FRAUD ALERT] Potential suspicious activity detected for Transfer {}", event.transferId());
                log.warn("Risk Score: {}/100, Analysis: {}", riskScore, response);
            } else {
                log.info("Fraud analysis completed for Transfer {}: Risk Score {}/100",
                        event.transferId(), riskScore);
            }
        } catch (Exception e) {
            log.error("Failed to analyze transaction {} for fraud", event.transferId(), e);
        }
    }

    private String buildFraudAnalysisPrompt(MoneyTransferredEvent event) {
        return String.format(
                "Analyze this transaction: Account %s moved $%s to Account %s. " +
                "Based on typical banking patterns, provide a risk score from 0-100 and a brief reason. " +
                "Respond with only the numeric score followed by a short explanation.",
                event.senderId(),
                event.amount(),
                event.receiverId()
        );
    }

    private int extractRiskScore(String response) {
        Matcher matcher = RISK_SCORE_PATTERN.matcher(response);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse risk score from AI response: {}", response);
                return 50; // Default to medium risk if parsing fails
            }
        }
        log.warn("No risk score found in AI response: {}", response);
        return 50; // Default to medium risk if no score found
    }
}
