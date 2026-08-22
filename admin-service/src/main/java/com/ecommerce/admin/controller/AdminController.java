package com.ecommerce.admin.controller;

import com.ecommerce.admin.dto.SystemSettingDTO;
import com.ecommerce.admin.dto.UserModerationRequest;
import com.ecommerce.admin.service.AdminService;
import com.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Endpoints for Platform Governance, User Moderation, and Dynamic System Settings")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/settings")
    @Operation(summary = "Update system setting", description = "Stores dynamic platform feature toggle or configuration in Redis cache")
    public ResponseEntity<ApiResponse<SystemSettingDTO>> updateSystemSetting(@Valid @RequestBody SystemSettingDTO setting) {
        SystemSettingDTO updated = adminService.updateSystemSetting(setting);
        return ResponseEntity.ok(ApiResponse.success(updated, "System setting updated successfully"));
    }

    @GetMapping("/settings/{key}")
    @Operation(summary = "Get system setting", description = "Retrieves dynamic system setting by key")
    public ResponseEntity<ApiResponse<SystemSettingDTO>> getSystemSetting(@PathVariable("key") String key) {
        SystemSettingDTO setting = adminService.getSystemSetting(key);
        return ResponseEntity.ok(ApiResponse.success(setting, "System setting retrieved successfully"));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get all system settings", description = "Lists all stored platform feature toggles")
    public ResponseEntity<ApiResponse<List<SystemSettingDTO>>> getAllSystemSettings() {
        List<SystemSettingDTO> settings = adminService.getAllSystemSettings();
        return ResponseEntity.ok(ApiResponse.success(settings, "All system settings retrieved successfully"));
    }

    @PostMapping("/users/moderate")
    @Operation(summary = "Moderate user account", description = "Bans, suspends, or unbans a user account and emits audit event to RabbitMQ audit.queue")
    public ResponseEntity<ApiResponse<Void>> moderateUser(@Valid @RequestBody UserModerationRequest request) {
        adminService.moderateUser(request);
        return ResponseEntity.ok(ApiResponse.success(null, "User moderation action processed successfully"));
    }
}
