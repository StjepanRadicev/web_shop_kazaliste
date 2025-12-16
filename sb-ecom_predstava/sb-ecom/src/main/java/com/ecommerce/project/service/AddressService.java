package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface AddressService {

    AddressDTO createAddress(@Valid AddressDTO addressDTO, User user);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAddressByUser(Long userId);

    AddressDTO patchedUpdatePerformance(Long addressId, Map<String, Object> patchPayLoad);
}
