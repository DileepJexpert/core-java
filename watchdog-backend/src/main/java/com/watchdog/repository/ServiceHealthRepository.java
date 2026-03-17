package com.watchdog.repository;

import com.watchdog.model.entity.ServiceHealthEntity;
import com.watchdog.model.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceHealthRepository extends JpaRepository<ServiceHealthEntity, String> {

    List<ServiceHealthEntity> findByStatus(ServiceStatus status);

    List<ServiceHealthEntity> findByStatusIn(List<ServiceStatus> statuses);
}
