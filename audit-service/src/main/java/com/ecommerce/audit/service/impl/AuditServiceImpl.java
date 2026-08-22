package com.ecommerce.audit.service.impl;

import com.ecommerce.audit.dto.LogEventRequest;
import com.ecommerce.audit.dto.SecurityEventDTO;
import com.ecommerce.audit.entity.SecurityEvent;
import com.ecommerce.audit.repository.SecurityEventRepository;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final SecurityEventRepository securityEventRepository;

    @Override
    @Transactional
    public SecurityEventDTO logSecurityEvent(LogEventRequest request) {
        log.info("Logging security event type: {} for userId: {}", request.getEventType(), request.getUserId());

        SecurityEvent event = SecurityEvent.builder()
                .userId(request.getUserId())
                .eventType(request.getEventType())
                .ipAddress(request.getIpAddress())
                .deviceInfo(request.getDeviceInfo())
                .success(request.getSuccess() != null ? request.getSuccess() : true)
                .failureReason(request.getFailureReason())
                .build();

        SecurityEvent saved = securityEventRepository.save(event);
        log.info("Security audit log saved with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SecurityEventDTO> getSecurityEventsByUserId(UUID userId, Pageable pageable) {
        Page<SecurityEvent> page = securityEventRepository.findByUserId(userId, pageable);
        List<SecurityEventDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<SecurityEventDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SecurityEventDTO> getSecurityEventsByType(String eventType, Pageable pageable) {
        Page<SecurityEvent> page = securityEventRepository.findByEventType(eventType, pageable);
        List<SecurityEventDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<SecurityEventDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    private SecurityEventDTO mapToDTO(SecurityEvent event) {
        return SecurityEventDTO.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .ipAddress(event.getIpAddress())
                .deviceInfo(event.getDeviceInfo())
                .success(event.getSuccess())
                .failureReason(event.getFailureReason())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
