package com.watchdog.repository;

import com.watchdog.model.entity.BaselineMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaselineMetricRepository extends JpaRepository<BaselineMetricEntity, Long> {

    Optional<BaselineMetricEntity> findByServiceNameAndMetricNameAndHourOfDayAndDayOfWeek(
            String serviceName, String metricName, int hourOfDay, int dayOfWeek);
}
