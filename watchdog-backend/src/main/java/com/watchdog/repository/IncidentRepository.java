package com.watchdog.repository;

import com.watchdog.model.entity.IncidentEntity;
import com.watchdog.model.enums.IncidentStatus;
import com.watchdog.model.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    List<IncidentEntity> findByStatusOrderByDetectedAtDesc(IncidentStatus status);

    List<IncidentEntity> findByServiceNameOrderByDetectedAtDesc(String serviceName);

    Page<IncidentEntity> findByStatusIn(List<IncidentStatus> statuses, Pageable pageable);

    boolean existsByServiceNameAndCorrelationRuleAndStatusIn(
            String serviceName, String correlationRule, List<IncidentStatus> statuses);

    @Query("SELECT i FROM IncidentEntity i WHERE i.detectedAt >= :since ORDER BY i.detectedAt DESC")
    List<IncidentEntity> findRecentIncidents(Instant since);

    @Query("SELECT COUNT(i) FROM IncidentEntity i WHERE i.serviceName = :service AND i.detectedAt >= :since")
    long countByServiceSince(String service, Instant since);

    @Query("SELECT i.serviceName, COUNT(i) FROM IncidentEntity i WHERE i.detectedAt >= :since GROUP BY i.serviceName ORDER BY COUNT(i) DESC")
    List<Object[]> countByServiceGrouped(Instant since);

    List<IncidentEntity> findByServiceNameAndDetectedAtAfterOrderByDetectedAtDesc(
            String serviceName, Instant after);
}
