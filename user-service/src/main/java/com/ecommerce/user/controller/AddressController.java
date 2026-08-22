package com.ecommerce.user.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    private UUID parseUserId(String header) {
        return UUID.fromString(header.replace("\"", "").trim());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getMyAddresses(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Addresses retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById(
            @PathVariable("id") UUID id,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        AddressDTO address = addressService.getAddressById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(address, "Address retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        AddressDTO created = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Address created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable("id") UUID id,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        UUID userId = parseUserId(userIdHeader);
        AddressDTO updated = addressService.updateAddress(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Address updated successfully"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable("id") UUID id,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        AddressDTO updated = addressService.setDefaultAddress(id, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Default address updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable("id") UUID id,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String userIdHeader
    ) {
        UUID userId = parseUserId(userIdHeader);
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}
