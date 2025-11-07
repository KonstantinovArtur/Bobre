// src/main/java/ru/forge/blacksmith_shop/audit/repo/LogsReadRepository.java
package ru.forge.blacksmith_shop.audit.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LogsReadRepository extends JpaRepository<ru.forge.blacksmith_shop.audit.domain.DummyLogEntity, Integer> {

    @Query(
            value = """
        SELECT 
            l.log_id         AS id,
            l.created_at     AS createdAt,
            l.user_id        AS userId,
            u.login          AS userLogin,
            o.operation_name AS operationName,
            l.details        AS details
        FROM logs l
        LEFT JOIN users u      ON u.user_id = l.user_id
        LEFT JOIN operations o ON o.operation_id = l.operation_id
        WHERE
            ( :q IS NULL OR :q = '' OR
              LOWER(
                COALESCE(l.details,'') || ' ' ||
                COALESCE(u.login,'')  || ' ' ||
                COALESCE(o.operation_name,'')
              ) LIKE LOWER(CONCAT('%', :q, '%'))
            )
          AND ( :userId IS NULL OR l.user_id = :userId )
          AND ( :actionName IS NULL OR :actionName = '' OR o.operation_name = :actionName )
          AND l.created_at >= COALESCE(CAST(:fromTs AS timestamptz), '-infinity'::timestamptz)
          AND l.created_at <  COALESCE(CAST(:toTs   AS timestamptz),  'infinity'::timestamptz)
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM logs l
        LEFT JOIN users u      ON u.user_id = l.user_id
        LEFT JOIN operations o ON o.operation_id = l.operation_id
        WHERE
            ( :q IS NULL OR :q = '' OR
              LOWER(
                COALESCE(l.details,'') || ' ' ||
                COALESCE(u.login,'')  || ' ' ||
                COALESCE(o.operation_name,'')
              ) LIKE LOWER(CONCAT('%', :q, '%'))
            )
          AND ( :userId IS NULL OR l.user_id = :userId )
          AND ( :actionName IS NULL OR :actionName = '' OR o.operation_name = :actionName )
          AND l.created_at >= COALESCE(CAST(:fromTs AS timestamptz), '-infinity'::timestamptz)
          AND l.created_at <  COALESCE(CAST(:toTs   AS timestamptz),  'infinity'::timestamptz)
        """,
            nativeQuery = true
    )
    Page<LogRow> findRows(
            @Param("q") String q,
            @Param("userId") Integer userId,
            @Param("actionName") String actionName,
            @Param("fromTs") Instant fromTs,   // <-- Instant
            @Param("toTs")   Instant toTs,     // <-- Instant
            Pageable pageable
    );
}
