package com.ecommerce.admin.service.impl;

import com.ecommerce.admin.dto.SystemSettingDTO;
import com.ecommerce.admin.dto.UserModerationRequest;
import com.ecommerce.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String SETTINGS_HASH_KEY = "system_settings";

    @Override
    public SystemSettingDTO updateSystemSetting(SystemSettingDTO setting) {
        log.info("Updating system setting key: {} = {}", setting.getKey(), setting.getValue());
        redisTemplate.opsForHash().put(SETTINGS_HASH_KEY, setting.getKey(), setting);

        Map<String, Object> auditEvent = Map.of(
                "eventType", "SYSTEM_SETTING_UPDATED",
                "key", setting.getKey(),
                "value", setting.getValue()
        );
        rabbitTemplate.convertAndSend("audit.exchange", "audit.system", auditEvent);

        return setting;
    }

    @Override
    public SystemSettingDTO getSystemSetting(String key) {
        Object val = redisTemplate.opsForHash().get(SETTINGS_HASH_KEY, key);
        if (val instanceof SystemSettingDTO dto) {
            return dto;
        }
        return SystemSettingDTO.builder()
                .key(key)
                .value("DEFAULT_ENABLED")
                .description("Default system configuration")
                .category("GLOBAL")
                .build();
    }

    @Override
    public List<SystemSettingDTO> getAllSystemSettings() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(SETTINGS_HASH_KEY);
        List<SystemSettingDTO> list = new ArrayList<>();
        for (Object obj : entries.values()) {
            if (obj instanceof SystemSettingDTO dto) {
                list.add(dto);
            }
        }
        return list;
    }

    @Override
    public void moderateUser(UserModerationRequest request) {
        log.info("Moderating user ID: {} with action: {}, reason: {}", request.getUserId(), request.getAction(), request.getReason());

        Map<String, Object> auditEvent = Map.of(
                "userId", request.getUserId().toString(),
                "eventType", "USER_MODERATED_" + request.getAction().toUpperCase(),
                "reason", request.getReason() != null ? request.getReason() : "Admin action"
        );
        rabbitTemplate.convertAndSend("audit.exchange", "audit.user", auditEvent);
        log.info("Dispatched user moderation audit event to RabbitMQ audit.queue");
    }
}
