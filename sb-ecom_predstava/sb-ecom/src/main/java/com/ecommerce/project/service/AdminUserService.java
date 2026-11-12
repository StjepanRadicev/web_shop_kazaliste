package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AdminUserDTO;
import com.ecommerce.project.repositories.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PerformanceRepository performanceRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(String search, Pageable pageable) {

        Page<User> users;

        if (search == null || search.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            String searchTerm = search.trim();

            users = userRepository
                    .findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            searchTerm,
                            searchTerm,
                            pageable
                    );
        }

        return users.map(user -> new AdminUserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getRoles().stream()
                        .map(role -> role.getRoleName().name())
                        .collect(Collectors.toSet())
        ));
    }


    @Transactional
    public void deleteUnverifiedUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "userId", userId
                        )
                );

        if (user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Nije moguće obrisati verificiranog korisnika."
            );
        }

        if (orderRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalStateException(
                    "Korisnik ima narudžbe i ne može se obrisati."
            );
        }

        if (performanceRepository.existsByUser_UserId(userId)) {
            throw new IllegalStateException(
                    "Korisnik ima povezane izvedbe i ne može se obrisati."
            );
        }

        Optional<Cart> cartOptional = cartRepository.findByUser_UserId(userId);

        if (cartOptional.isPresent()
                && !cartOptional.get().getCartItems().isEmpty()) {
            throw new IllegalStateException(
                    "Korisnik ima stavke u košarici i ne može se obrisati."
            );
        }

        List<Address> addresses = addressRepository.findByUser_UserId(userId);

        for (Address address : addresses) {
            if (orderRepository.existsByAddress_AddressId(address.getAddressId())) {
                throw new IllegalStateException(
                        "Korisnik ima adresu povezanu s narudžbom."
                );
            }
        }

        // Sve provjere su prošle.

        emailVerificationTokenRepository.deleteByUser_UserId(userId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            user.setCart(null);
            cartRepository.delete(cart);
        }

        for (Address address : addresses) {
            user.getAddresses().remove(address);
            address.setUser(null);
        }

        userRepository.delete(user);
        userRepository.flush();
    }
}