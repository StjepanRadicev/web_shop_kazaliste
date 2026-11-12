package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {

    private Long userId;

    private String userName;

    private String email;

    private boolean emailVerified;

    private Set<String> roles;
}
