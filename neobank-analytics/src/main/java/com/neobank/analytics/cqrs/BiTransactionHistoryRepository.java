package com.neobank.analytics.cqrs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for BI transaction history with analytics queries.
 */
public interface BiTransactionHistoryRepository extends JpaRepository<BiTransactionHistory, UUID> {

    List<BiTransactionHistory> findByFromAccountIdOrderByOccurredAtDesc(UUID accountId);

    List<BiTransactionHistory> findByToAccountIdOrderByOccurredAtDesc(UUID accountId);

    @Query("SELECT SUM(t.amount) FROM BiTransactionHistory t WHERE t.fromAccountId = :accountId AND t.occurredAt BETWEEN :start AND :end")
    BigDecimal getTotalOutflow(@Param("accountId") UUID accountId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT SUM(t.amount) FROM BiTransactionHistory t WHERE t.toAccountId = :accountId AND t.occurredAt BETWEEN :start AND :end")
    BigDecimal getTotalInflow(@Param("accountId") UUID accountId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT t.currency, SUM(t.amount) FROM BiTransactionHistory t WHERE t.occurredAt BETWEEN :start AND :end GROUP BY t.currency")
    List<Object[]> getVolumeByCurrency(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DATE(t.occurredAt), COUNT(t) FROM BiTransactionHistory t WHERE t.occurredAt BETWEEN :start AND :end GROUP BY DATE(t.occurredAt)")
    List<Object[]> getDailyTransactionCount(@Param("start") Instant start, @Param("end") Instant end);
}
