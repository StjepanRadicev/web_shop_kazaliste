package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressPatchRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressService {

    AddressDTO createAddress(@Valid AddressDTO addressDTO, User user);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAddressByUser(Long userId);

    @Transactional
    AddressDTO updateAddress(Long addressId, AddressPatchRequest request);

    AddressDTO deleteAddress(Long addressId);
}
