package com.ecommerce.project.controller;


import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressPatchRequest;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class AddressController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressService addressService;



    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {

        User user = authUtil.loggedInUser();

        AddressDTO savedAddress = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<AddressDTO>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {

        List<AddressDTO> addressDTOS = addressService.getAllAddresses();

        return new ResponseEntity<List<AddressDTO>>(addressDTOS, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {

        AddressDTO addressDTO = addressService.getAddressById(addressId);

        return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getAddressByUser() {
        Long userId = authUtil.loggedInUserId();

        List<AddressDTO> addressDTOList = addressService.getAddressByUser(userId);

        return new ResponseEntity<List<AddressDTO>>(addressDTOList, HttpStatus.OK);
    }

    @PatchMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@RequestBody AddressPatchRequest request,
                                                    @PathVariable Long addressId) {

        AddressDTO addressDTO = addressService.updateAddress(addressId, request);

        return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddress(@PathVariable Long addressId) {

        AddressDTO addressDTO = addressService.deleteAddress(addressId);

        return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
    }
}














