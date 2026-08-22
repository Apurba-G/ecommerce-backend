package com.ecommerce.user.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
