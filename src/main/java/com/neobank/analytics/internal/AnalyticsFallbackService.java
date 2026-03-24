package com.neobank.analytics.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fallback service for analytics operations.
 * Queues events locally when the analytics module is unavailable.
 * Implements the Circuit Breaker Fallback pattern.
 */
@Service
public class AnalyticsFallbackService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsFallbackService.class);
    private static final int MAX_QUEUE_SIZE = 10000;

    /**
     * Local queue for analytics events when analytics module is down.
     * Events are stored in memory and can be replayed later.
     */
    private final ConcurrentHashMap<String, List<QueuedAnalyticsEvent>> eventQueue;
    private final ExecutorService replayExecutor;

    public AnalyticsFallbackService() {
        this.eventQueue = new ConcurrentHashMap<>();
        this.replayExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "analytics-replay-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Queue an analytics event locally when the analytics module is unavailable.
     * This is the fallback method called by the circuit breaker.
     */
    public void queueEventForLater(String eventType, Map<String, Object> eventData) {
        String eventId = UUID.randomUUID().toString();
        QueuedAnalyticsEvent queuedEvent = new QueuedAnalyticsEvent(
            eventId,
            eventType,
            eventData,
            System.currentTimeMillis()
        );

        eventQueue.computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(queuedEvent);

        log.warn("Analytics module unavailable. Event queued for later replay: {} (Queue size: {})", 
            eventType, getQueueSize());

        // If queue is getting too large, trigger a warning
        if (getQueueSize() > MAX_QUEUE_SIZE * 0.8) {
            log.error("Analytics event queue is 80% full! Current size: {}", getQueueSize());
        }
    }

    /**
     * Queue a transfer analytics event (specific fallback for transfer operations).
     */
    public void queueTransferEvent(Map<String, Object> transferData) {
        queueEventForLater("TRANSFER_ANALYTICS", transferData);
    }

    /**
     * Queue a user activity event.
     */
    public void queueUserActivityEvent(Map<String, Object> activityData) {
        queueEventForLater("USER_ACTIVITY", activityData);
    }

    /**
     * Get the current queue size across all event types.
     */
    public int getQueueSize() {
        return eventQueue.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    /**
     * Get queued events by type.
     */
    public List<QueuedAnalyticsEvent> getQueuedEvents(String eventType) {
        return eventQueue.getOrDefault(eventType, new ArrayList<>());
    }

    /**
     * Clear queued events after successful replay.
     */
    public void clearQueuedEvents(String eventType) {
        eventQueue.remove(eventType);
        log.info("Cleared queued events for type: {}", eventType);
    }

    /**
     * Get queue statistics for monitoring.
     */
    public Map<String, Integer> getQueueStats() {
        return eventQueue.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }

    /**
     * Trigger async replay of queued events.
     * Should be called when analytics module becomes available again.
     */
    public void triggerReplay(AnalyticsEventReplayer replayer) {
        replayExecutor.submit(() -> {
            log.info("Starting replay of {} queued analytics events", getQueueSize());
            
            eventQueue.forEach((eventType, events) -> {
                log.info("Replaying {} events of type {}", events.size(), eventType);
                
                for (QueuedAnalyticsEvent event : events) {
                    try {
                        replayer.replay(event);
                    } catch (Exception e) {
                        log.error("Failed to replay event: {}", event.eventId, e);
                    }
                }
                
                clearQueuedEvents(eventType);
            });
            
            log.info("Replay completed");
        });
    }

    /**
     * Functional interface for replaying events.
     */
    @FunctionalInterface
    public interface AnalyticsEventReplayer {
        void replay(QueuedAnalyticsEvent event) throws Exception;
    }

    /**
     * Record representing a queued analytics event.
     */
    public record QueuedAnalyticsEvent(
        String eventId,
        String eventType,
        Map<String, Object> eventData,
        long queuedAt
    ) {}
}
