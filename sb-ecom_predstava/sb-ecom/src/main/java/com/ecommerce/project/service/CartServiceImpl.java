package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.Performance;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.PerformanceDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.PerformanceRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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




    @Override
    public CartDTO addPerformanceToCart(Long performanceId, Integer quantity) {


        // Find existing cart or create one
        Cart cart = createCart();

        // Retrieve Product Details
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        int performanceQuantity = performanceAvailabilityService.getAvailability(performanceId);

        // Perform Validations
        CartItem cartItem = cartItemRepository.findCartItemByPerformanceIdAndCartId(cart.getCartId(), performanceId);

        if (cartItem != null) {
            throw new APIException("Performance " + performance.getPerformanceName() + " already exists in the cart.");
        }

        if (performanceQuantity == 0) {
            throw new APIException(performance.getPerformanceName() + " is not available");
        }

        if (performanceQuantity < quantity) {
            throw new APIException("Please, make an order of the " + performance.getPerformanceName()
            + " less than or equal to the quantity " + performanceQuantity + ".");
        }

        // Create Cart Item
        CartItem newCartItem = new CartItem();
        newCartItem.setPerformance(performance);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(performance.getDiscount());
        newCartItem.setPerformancePrice(performance.getSpecialPrice());

        // Save Cart Item
        cartItemRepository.save(newCartItem);

        //performance.setQuantity(performance.getQuantity() - quantity);

        cart.setTotalPrice(cart.getTotalPrice() + (performance.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        // Return updated cart
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<PerformanceDTO> performances = cart.getCartItems().stream()
                .map(item -> {
                    PerformanceDTO dto = modelMapper.map(item.getPerformance(), PerformanceDTO.class);
                    dto.setQuantityInCart(item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());

        cartDTO.setPerformances(performances);
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

            List<PerformanceDTO> performances = cart.getCartItems().stream()
                    .map(item -> {
                        PerformanceDTO performanceDTO = modelMapper.map(item.getPerformance(), PerformanceDTO.class);
                        performanceDTO.setQuantityInCart(item.getQuantity()); // količina u korpi
                        return performanceDTO;
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

        List<PerformanceDTO> products = cart.getCartItems().stream()
                .map(item -> {
                    PerformanceDTO productDTO = modelMapper.map(item.getPerformance(), PerformanceDTO.class);
                    productDTO.setQuantityInCart(item.getQuantity()); // količina u korpi
                    return productDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setPerformances(products);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updatePerformanceQuantityInCart(Long performanceId, Integer quantity) {


        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performanceId", performanceId));

        int performanceQuantity = performanceAvailabilityService.getAvailability(performanceId);

        CartItem cartItem = cartItemRepository.findCartItemByPerformanceIdAndCartId(cartId, performanceId);
        if (cartItem == null) {
            throw new APIException("Performance " + performance.getPerformanceName() + " not available in the cart!");
        }

        // Calculate new quantity
        int newQuantity = cartItem.getQuantity() + quantity;

        // Validation to prevent negative quantities
        if(newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative");
        }

        if (quantity > 0) {
            if (performanceQuantity == 0) {
                throw new APIException(performance.getPerformanceName() + " is not available");
            }

            if (performanceQuantity < quantity) {
                throw new APIException("Please, make an order of the " + performance.getPerformanceName()
                        + " less than or equal to the quantity " + performanceQuantity + ".");
            }
        }


        if ( newQuantity == 0) {
            deletePerformanceFromCart(cartId, performanceId);
        } else {

            cartItem.setPerformancePrice(performance.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(performance.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getPerformancePrice() * quantity));

            //performance.setQuantity(performance.getQuantity() - quantity);

            cartRepository.save(cart);
            CartItem updatedItem = cartItemRepository.save(cartItem);
        }



        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<PerformanceDTO> products = cart.getCartItems().stream()
                .map(item -> {
                    PerformanceDTO productDTO = modelMapper.map(item.getPerformance(), PerformanceDTO.class);
                    productDTO.setQuantityInCart(item.getQuantity()); // količina u korpi
                    return productDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setPerformances(products);

        return cartDTO;
    }

    @Transactional
    @Override
    public String deletePerformanceFromCart(Long cartId, Long performanceId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByPerformanceIdAndCartId(cartId, performanceId);

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performance", performanceId));

        if (cartItem == null) {
            throw new ResourceNotFoundException("Performance", "performanceId", performanceId);
        }

        cart.setTotalPrice((cart.getTotalPrice() -
                (cartItem.getPerformancePrice()) * cartItem.getQuantity()));


        //performance.setQuantity(performance.getQuantity() + cartItem.getQuantity());

        cartItemRepository.deleteCartItemByPerformanceIdAndCartId(cartId, performanceId);

        return "Product " + cartItem.getPerformance().getPerformanceName() + " removed from the cart.";
    }

    @Override
    public void updatePerformanceInCarts(Long cartId, Long performanceId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "performanceId", performanceId));


        CartItem cartItem = cartItemRepository.findCartItemByPerformanceIdAndCartId(cartId, performanceId);

        if (cartItem == null) {
            throw new APIException("Performance " + performance.getPerformanceName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice() -
                (cartItem.getPerformancePrice() * cartItem.getQuantity());

        cartItem.setPerformancePrice(performance.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getPerformancePrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);

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
}


























