package com.ecommerce.user.service;

import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.dto.AddressDTO;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressDTO> getAddressesByUserId(UUID userId);

    AddressDTO getAddressById(UUID addressId, UUID userId);

    AddressDTO createAddress(UUID userId, AddressCreateRequest request);

    AddressDTO updateAddress(UUID addressId, UUID userId, AddressCreateRequest request);

    void deleteAddress(UUID addressId, UUID userId);

    AddressDTO setDefaultAddress(UUID addressId, UUID userId);
}
