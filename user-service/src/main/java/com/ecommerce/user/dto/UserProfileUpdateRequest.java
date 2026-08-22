package com.ecommerce.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
}
