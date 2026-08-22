package com.ecommerce.user.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByUserId(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getAddressById(UUID addressId, UUID userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return mapToDTO(address);
    }

    @Override
    @Transactional
    public AddressDTO createAddress(UUID userId, AddressCreateRequest request) {
        // If this is the user's first address, make it default automatically
        boolean isFirst = addressRepository.findByUserId(userId).isEmpty();

        Address address = Address.builder()
                .userId(userId)
                .addressType(request.getAddressType() != null ? request.getAddressType() : "HOME")
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPostalCode())
                .isDefault(isFirst || request.isDefault())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Address saved = addressRepository.save(address);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(UUID addressId, UUID userId, AddressCreateRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        address.setAddressType(request.getAddressType());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setDefault(request.isDefault());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        Address updated = addressRepository.save(address);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddress(UUID addressId, UUID userId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        address.setDefault(true);
        Address updated = addressRepository.save(address);
        return mapToDTO(updated);
    }

    private AddressDTO mapToDTO(Address entity) {
        return AddressDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .addressType(entity.getAddressType())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .isDefault(entity.isDefault())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}
