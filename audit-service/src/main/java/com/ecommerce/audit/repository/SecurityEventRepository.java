package com.ecommerce.audit.repository;

import com.ecommerce.audit.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByUserId(UUID userId, Pageable pageable);

    Page<SecurityEvent> findByEventType(String eventType, Pageable pageable);
}
