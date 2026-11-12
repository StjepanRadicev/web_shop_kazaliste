package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AdminUserDTO;
import com.ecommerce.project.service.AdminUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;


    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String search) {

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by("userId").descending()
        );

        Page<AdminUserDTO> users = adminUserService.getAllUsers(
                search,
                pageable
        );

        return ResponseEntity.ok(users);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUnverifiedUser(
            @PathVariable Long userId) {

        adminUserService.deleteUnverifiedUser(userId);

        return ResponseEntity.noContent().build();
    }


}