package com.ecommerce.admin.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettingDTO implements Serializable {
    private String key;
    private String value;
    private String description;
    private String category;
}
