package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PerformanceSeatRepository performanceseatRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        // Getting User Cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // Create a new order with payment info

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        // Get items from the cart into order items
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();

            if(cartItem.getPerformanceSeat().getStatus().equals(PerformanceSeatStatus.AVAILABLE)
                    || cartItem.getPerformanceSeat().getHeldUntil().isBefore(LocalDateTime.now())
                    || !cartItem.getPerformanceSeat().getHeldByCartId().equals(cart.getCartId())) {
                throw new APIException("Molim da ponovo dodate sjedalo u košaricu!");
            }

            orderItem.setPerformanceSeat(cartItem.getPerformanceSeat());

            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedPerformancePrice(cartItem.getPerformancePrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);


        // Update products stock
        cart.getCartItems().forEach(item -> {
//
            PerformanceSeat performanceSeat = item.getPerformanceSeat();

            performanceSeat.setStatus(PerformanceSeatStatus.SOLD);

            performanceseatRepository.save(performanceSeat);

            // Clear the cart
            cartService.deletePerformanceFromCart(cart.getCartId(), item.getPerformanceSeat().getPerformanceSeatId());
        });

      // Send back the order summary
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);

        orderDTO.setAddressId(addressId);

        List<OrderItemDTO> itemDTOs = cart.getCartItems().stream()
                .map(ci -> {
                    OrderItemDTO dto = new OrderItemDTO();
                    dto.setPerformanceName(ci.getPerformanceSeat().getPerformance().getPerformanceName());
                    dto.setRowLabel(ci.getPerformanceSeat().getSeat().getRowLabel());
                    dto.setSection(ci.getPerformanceSeat().getSeat().getSection());
                    dto.setOrderedPerformancePrice(ci.getPerformancePrice());
                    dto.setDiscount(ci.getDiscount());
                    return dto;
                })
                .toList();

        orderDTO.setOrderItems(itemDTOs);

        return orderDTO;
    }
}



































