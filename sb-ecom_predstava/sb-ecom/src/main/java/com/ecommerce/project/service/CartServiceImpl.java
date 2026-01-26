package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    PerformanceRepository performanceRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PerformanceAvailabilityService performanceAvailabilityService;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    PerformanceSeatRepository performanceSeatRepository;


    @Transactional
    @Override
    public CartDTO addSeatToCart(Long performanceSeatId) {

        // Find existing cart or create one
        Cart cart = createCart();

        PerformanceSeat performanceSeat = performanceSeatRepository.findByIdForUpdate(performanceSeatId)
                .orElseThrow(() -> new RuntimeException("PerformanceSeat not found"));


        // if  HELD still exists but its same cart => idempotentno (ne dupliraj)
        if (performanceSeat.getStatus() == PerformanceSeatStatus.HELD
                && cart.getCartId().equals(performanceSeat.getHeldByCartId())) {
            throw  new APIException("Performance seat already exist in the cart");
        }

        // expired hold => release
        if (performanceSeat.getStatus() == PerformanceSeatStatus.HELD
                && performanceSeat.getHeldUntil() != null
                && performanceSeat.getHeldUntil().isBefore(LocalDateTime.now())) {
            performanceSeat.setStatus(PerformanceSeatStatus.AVAILABLE);
            performanceSeat.setHeldUntil(null);
            performanceSeat.setHeldByCartId(null);
        }

        if (performanceSeat.getStatus() != PerformanceSeatStatus.AVAILABLE) {
            throw new APIException("Seat not available");
        }


        // 1) hold in base
        performanceSeat.setStatus(PerformanceSeatStatus.HELD);
        performanceSeat.setHeldByCartId(cart.getCartId());
        performanceSeat.setHeldUntil(LocalDateTime.now().plusMinutes(10));

        performanceSeatRepository.save(performanceSeat);


        // Create Cart Item
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setDiscount(performanceSeat.getPerformance().getDiscount());
        cartItem.setPerformanceSeat(performanceSeat);
        cartItem.setPerformancePrice(performanceSeat.getPrice());
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(cart.getTotalPrice() + performanceSeat.getPrice());

        cartRepository.save(cart);

        // Return updated cart
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<PerformanceSeatDTO> performanceSeatDTOS = cart.getCartItems().stream()
                .map(item ->{
                    PerformanceSeatDTO dto = modelMapper.map(item.getPerformanceSeat(), PerformanceSeatDTO.class );
                    dto.setRowLabel(item.getPerformanceSeat().getSeat().getRowLabel());
                    dto.setSection(item.getPerformanceSeat().getSeat().getSection());
                    return dto;
                })
                .collect(Collectors.toList());

        cartDTO.setPerformances(performanceSeatDTOS);
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {

        List<Cart> carts = cartRepository.findAll();

        if(carts.isEmpty()) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<PerformanceSeatDTO> performances = cart.getCartItems().stream()
                    .map(item -> {
                        PerformanceSeatDTO performanceSeatDTO = modelMapper.map(item.getPerformanceSeat(), PerformanceSeatDTO.class);
                        performanceSeatDTO.setRowLabel(item.getPerformanceSeat().getSeat().getRowLabel());
                        performanceSeatDTO.setSection(item.getPerformanceSeat().getSeat().getSection());
                        return performanceSeatDTO;
                    })
                    .collect(Collectors.toList());

            cartDTO.setPerformances(performances);
            return cartDTO;
        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {

        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<PerformanceSeatDTO> performances = cart.getCartItems().stream()
                .map(item -> {
                    PerformanceSeatDTO performanceSeatDTO = modelMapper.map(item.getPerformanceSeat(), PerformanceSeatDTO.class);
                    performanceSeatDTO.setRowLabel(item.getPerformanceSeat().getSeat().getRowLabel());
                    performanceSeatDTO.setSection(item.getPerformanceSeat().getSeat().getSection());
                    return performanceSeatDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setPerformances(performances);

        return cartDTO;
    }

    @Override
    public CartDTO updatePerformanceQuantityInCart(Long performanceId, Integer quantity) {
        return null;
    }

    @Transactional
    @Override
    public String deletePerformanceFromCart(Long cartId, Long performanceSeatId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByPerformanceSeatIdAndCartId(cartId, performanceSeatId);

        PerformanceSeat performanceSeat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceSeat", "performanceSeatId", performanceSeatId));

        if (cartItem == null) {
            throw new ResourceNotFoundException("PerformanceSeat", "performanceSeatId", performanceSeatId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getPerformancePrice() );

        cartItemRepository.deleteCartItemByPerformanceSeatIdAndCartId(cartId, performanceSeatId);

        if(performanceSeat.getStatus().equals(PerformanceSeatStatus.HELD)) {
            performanceSeat.setStatus(PerformanceSeatStatus.AVAILABLE);
            performanceSeat.setHeldByCartId(null);
            performanceSeat.setHeldUntil(null);
        }

        performanceSeatRepository.save(performanceSeat);

        return "PerformanceSeat " + cartItem.getPerformanceSeat().getSeat() + " removed from the cart.";
    }

    @Override
    public void updatePerformanceInCarts(Long cartId, Long performanceId) {

    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());

        return cartRepository.save(cart);
    }

    @Transactional
    public void cleanupExpiredHeldItems() {
        LocalDateTime now = LocalDateTime.now();
        var expired = cartItemRepository.findExpiredHeldItems(now);

        for (var e : expired) {
            deletePerformanceFromCart(e.getCartId(), e.getPerformanceSeatId());
        }
    }
}



























