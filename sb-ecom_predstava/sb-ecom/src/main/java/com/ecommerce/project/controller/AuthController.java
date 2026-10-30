package com.ecommerce.project.controller;


import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final JwtUtils jwtUtils;


    private final UserRepository userRepository;


    private final PasswordEncoder encoder;


    private final RoleRepository roleRepository;

    @Autowired
    private AuthService authService;



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        AuthenticationResult result = authService.login(loginRequest);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                result.getJwtCookie().toString())
                .body(result.getResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        Map<String, String> errors = new HashMap<>();

        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            errors.put("username", "Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            errors.put("email", "Email is already taken!");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("errors", errors));
        }


        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not Found!"));

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return ResponseEntity.ok(
                new MessageResponse("User registered successfully!")
        );

//        if(userRepository.existsByUserName(signupRequest.getUsername()) ) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Username is already taken!"));
//        };
//
//        if(userRepository.existsByEmail(signupRequest.getEmail()) ) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Email is already taken!"));
//        };
//
//        User user = new User(
//                signupRequest.getUsername(),
//                signupRequest.getEmail(),
//                encoder.encode(signupRequest.getPassword())
//        );
//
//        Set<String> strRole = signupRequest.getRole();
//        Set<Role> roles = new HashSet<>();
//
//        if (strRole == null) {
//            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not Found!"));
//            roles.add(userRole);
//        } else {
//            strRole.forEach(role -> {
//                switch (role) {
//                    case "admin":
//                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not Found!"));
//                        roles.add(adminRole);
//                        break;
//                    case "seller":
//                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not Found!"));
//                        roles.add(sellerRole);
//                        break;
//                    default:
//                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not Found!"));
//                        roles.add(userRole);
//                }
//            });
//        }
//
//        user.setRoles(roles);
//        userRepository.save(user);
//
//        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return null;
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),
                userDetails.getUsername(), roles);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}

























