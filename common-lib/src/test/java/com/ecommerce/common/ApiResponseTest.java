package com.ecommerce.common;

import com.ecommerce.common.response.ApiError;
import com.ecommerce.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    @DisplayName("Should create successful ApiResponse")
    void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Test Payload", "Success message");

        assertTrue(response.isSuccess());
        assertEquals("Test Payload", response.getData());
        assertEquals("Success message", response.getMessage());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Should create error ApiResponse")
    void testErrorResponse() {
        ApiError error = ApiError.builder()
                .errorCode("ERR_NOT_FOUND")
                .errorMessage("Resource not found")
                .build();

        ApiResponse<Void> response = ApiResponse.failure(error);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals("ERR_NOT_FOUND", response.getError().getErrorCode());
        assertEquals("Resource not found", response.getError().getErrorMessage());
    }
}
