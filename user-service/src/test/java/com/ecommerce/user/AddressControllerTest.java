package com.ecommerce.user;

import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.user.controller.AddressController;
import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/users/addresses should return list of addresses")
    void testGetAddresses() throws Exception {
        UUID userId = UUID.randomUUID();
        AddressDTO addressDTO = AddressDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Alex Morgan")
                .addressLine1("452 Silicon Ave")
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .build();

        when(addressService.getAddressesByUserId(eq(userId))).thenReturn(List.of(addressDTO));

        mockMvc.perform(get("/api/v1/users/addresses")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("Alex Morgan"));
    }

    @Test
    @DisplayName("POST /api/v1/users/addresses should create address and return 201")
    void testCreateAddress() throws Exception {
        UUID userId = UUID.randomUUID();
        AddressCreateRequest request = AddressCreateRequest.builder()
                .addressType("HOME")
                .fullName("Alex Morgan")
                .phone("+1234567890")
                .addressLine1("452 Silicon Ave")
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .build();

        AddressDTO createdDTO = AddressDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Alex Morgan")
                .addressLine1("452 Silicon Ave")
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .isDefault(true)
                .build();

        when(addressService.createAddress(eq(userId), any(AddressCreateRequest.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/api/v1/users/addresses")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Alex Morgan"));
    }
}
