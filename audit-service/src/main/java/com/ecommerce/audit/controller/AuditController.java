package com.ecommerce.audit.controller;

import com.ecommerce.audit.dto.LogEventRequest;
import com.ecommerce.audit.dto.SecurityEventDTO;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Controller", description = "Endpoints for Security Compliance Logging and Event Querying")
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/events")
    @Operation(summary = "Log security audit event", description = "Logs a security event e.g. LOGIN_SUCCESS, PASSWORD_CHANGE")
    public ResponseEntity<ApiResponse<SecurityEventDTO>> logSecurityEvent(@Valid @RequestBody LogEventRequest request) {
        SecurityEventDTO event = auditService.logSecurityEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(event, "Audit event logged successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs for user", description = "Retrieves paged security events for a specific user")
    public ResponseEntity<ApiResponse<PagedResponse<SecurityEventDTO>>> getSecurityEventsByUserId(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<SecurityEventDTO> events = auditService.getSecurityEventsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events, "User security logs retrieved successfully"));
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "Get audit logs by event type", description = "Retrieves paged audit events filtered by type e.g. LOGIN_FAILED")
    public ResponseEntity<ApiResponse<PagedResponse<SecurityEventDTO>>> getSecurityEventsByType(
            @PathVariable("eventType") String eventType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<SecurityEventDTO> events = auditService.getSecurityEventsByType(eventType, pageable);
        return ResponseEntity.ok(ApiResponse.success(events, "Audit logs retrieved successfully"));
    }
}
