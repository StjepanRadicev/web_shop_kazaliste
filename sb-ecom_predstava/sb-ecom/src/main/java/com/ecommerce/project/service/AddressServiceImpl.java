package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressPatchRequest;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {

        List<Address> addressList = addressRepository.findAll();

//        if(addressList.isEmpty()) {
//            throw new ResourceNotFoundException();
//        }

        return addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressByUser(Long userId) {

        List<Address> addressList = addressRepository.findByUser_UserId(userId);

        return addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Transactional
    @Override
    public AddressDTO updateAddress(Long addressId, AddressPatchRequest request) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getZipCode() != null) address.setZipCode(request.getZipCode());
        if (request.getBuildingName() != null) address.setBuildingName(request.getBuildingName());
        if (request.getState() != null) address.setState(request.getState());

        addressRepository.save(address);

        User user = address.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        user.getAddresses().add(address);
        userRepository.save(user);

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = address.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(address);

        return modelMapper.map(address, AddressDTO.class);
    }


}
































