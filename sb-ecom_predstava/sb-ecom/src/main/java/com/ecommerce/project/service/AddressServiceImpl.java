package com.ecommerce.project.service;

import ch.qos.logback.core.read.ListAppender;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.helper.JsonPatchUtils;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Performance;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;


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

    @Override
    public AddressDTO patchedUpdatePerformance(Long addressId, Map<String, Object> patchPayLoad) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // if exists Id in JSON request body
        if(JsonPatchUtils.containsAnyIdKey(patchPayLoad)) {
            throw new APIException("Product id not allowed in request body ");
        }

        Address patchedAddress = apply(patchPayLoad, address);

        Address savedAddress  = addressRepository.save(patchedAddress);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    //method for patch update
    public Address apply(Map<String, Object> patchPayLoad, Address tempPerformance) {

        ObjectNode performanceNode = objectMapper.convertValue(tempPerformance, ObjectNode.class);

        ObjectNode patchNode = objectMapper.convertValue(patchPayLoad, ObjectNode.class);

        performanceNode.setAll(patchNode);

        return objectMapper.convertValue(performanceNode, Address.class);
    }

}
































