package com.ecommerce.auth.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String accountStatus;
    private List<String> roles;
}
