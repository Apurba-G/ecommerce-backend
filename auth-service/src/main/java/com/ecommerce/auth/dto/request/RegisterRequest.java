package com.ecommerce.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    private String firstName;
    private String lastName;
    private String phone;

    /**
     * Optional role to assign (e.g., "ROLE_SELLER", "ROLE_CUSTOMER", "ROLE_ADMIN").
     * Defaults to "ROLE_CUSTOMER" if omitted.
     */
    private String role;
}
