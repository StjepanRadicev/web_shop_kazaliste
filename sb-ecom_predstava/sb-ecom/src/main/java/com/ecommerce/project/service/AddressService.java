package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressPatchRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {

    AddressDTO createAddress(@Valid AddressDTO addressDTO, User user);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAddressByUser(Long userId);

    AddressDTO updateAddress(Long addressId, AddressPatchRequest request);

    AddressDTO deleteAddress(Long addressId);
}
