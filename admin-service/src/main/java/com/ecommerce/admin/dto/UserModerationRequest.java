package com.ecommerce.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModerationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Action is required (e.g. BAN, UNBAN, SUSPEND)")
    private String action;

    private String reason;
}
