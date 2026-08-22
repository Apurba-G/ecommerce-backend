package com.ecommerce.audit.service;

import com.ecommerce.audit.dto.LogEventRequest;
import com.ecommerce.audit.dto.SecurityEventDTO;
import com.ecommerce.common.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {

    SecurityEventDTO logSecurityEvent(LogEventRequest request);

    PagedResponse<SecurityEventDTO> getSecurityEventsByUserId(UUID userId, Pageable pageable);

    PagedResponse<SecurityEventDTO> getSecurityEventsByType(String eventType, Pageable pageable);
}
