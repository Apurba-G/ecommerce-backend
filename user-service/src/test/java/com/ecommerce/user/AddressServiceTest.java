package com.ecommerce.user;

import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private UUID userId;
    private Address sampleAddress;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleAddress = Address.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .addressType("HOME")
                .fullName("Alex Morgan")
                .phone("+1234567890")
                .addressLine1("452 Silicon Ave")
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .postalCode("560100")
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("Should create address and automatically set default if first address")
    void testCreateFirstAddressSetsDefault() {
        AddressCreateRequest request = AddressCreateRequest.builder()
                .addressType("HOME")
                .fullName("Alex Morgan")
                .phone("+1234567890")
                .addressLine1("452 Silicon Ave")
                .city("Bengaluru")
                .state("Karnataka")
                .country("India")
                .postalCode("560100")
                .isDefault(false)
                .build();

        when(addressRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        AddressDTO dto = addressService.createAddress(userId, request);

        assertNotNull(dto);
        assertTrue(dto.isDefault());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    @DisplayName("Should retrieve list of addresses for user")
    void testGetAddressesByUserId() {
        when(addressRepository.findByUserId(userId)).thenReturn(List.of(sampleAddress));

        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
        assertEquals("Alex Morgan", addresses.get(0).getFullName());
    }

    @Test
    @DisplayName("Should delete address successfully")
    void testDeleteAddress() {
        UUID addressId = sampleAddress.getId();
        when(addressRepository.findByIdAndUserId(addressId, userId)).thenReturn(Optional.of(sampleAddress));

        addressService.deleteAddress(addressId, userId);

        verify(addressRepository, times(1)).delete(sampleAddress);
    }
}
