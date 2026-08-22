package com.ecommerce.admin.service;

import com.ecommerce.admin.dto.SystemSettingDTO;
import com.ecommerce.admin.dto.UserModerationRequest;

import java.util.List;

public interface AdminService {

    SystemSettingDTO updateSystemSetting(SystemSettingDTO setting);

    SystemSettingDTO getSystemSetting(String key);

    List<SystemSettingDTO> getAllSystemSettings();

    void moderateUser(UserModerationRequest request);
}
