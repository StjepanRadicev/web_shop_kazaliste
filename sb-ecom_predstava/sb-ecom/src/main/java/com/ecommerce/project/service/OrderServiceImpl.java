package com.ecommerce.project.service;

import com.ecommerce.project.email.EmailService;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private StripeService stripeService;

    @Autowired
    private  EmailService emailService;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
//        // Getting User Cart
//        Cart cart = cartRepository.findCartByEmail(emailId);
//        if (cart == null) {
//            throw new ResourceNotFoundException("Cart", "email", emailId);
//        }
//
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
//
//        // Create a new order with payment info
//
//        Order order = new Order();
//        order.setEmail(emailId);
//        order.setOrderDate(LocalDateTime.now());
//        order.setTotalAmount(cart.getTotalPrice());
//        order.setOrderStatus("Order Accepted !");
//        order.setAddress(address);
//
//        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
//        payment.setOrder(order);
//        payment = paymentRepository.save(payment);
//        order.setPayment(payment);
//
//        Order savedOrder = orderRepository.save(order);
//
//        // Get items from the cart into order items
//        List<CartItem> cartItems = cart.getCartItems();
//        if (cartItems.isEmpty()) {
//            throw new APIException("Cart is empty");
//        }
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        for(CartItem cartItem : cartItems) {
//            OrderItem orderItem = new OrderItem();
//
//            if(cartItem.getPerformanceSeat().getStatus().equals(PerformanceSeatStatus.AVAILABLE)
//                    || cartItem.getPerformanceSeat().getHeldUntil().isBefore(LocalDateTime.now())
//                    || !cartItem.getPerformanceSeat().getHeldByCartId().equals(cart.getCartId())) {
//                throw new APIException("Molim da ponovo dodate sjedalo u košaricu!");
//            }
//
//            orderItem.setPerformanceSeat(cartItem.getPerformanceSeat());
//
//            orderItem.setDiscount(cartItem.getDiscount());
//            orderItem.setOrderedPerformancePrice(cartItem.getPerformancePrice());
//            orderItem.setOrder(savedOrder);
//            orderItems.add(orderItem);
//        }
//
//        orderItems = orderItemRepository.saveAll(orderItems);
//
//
//        // Update performanceSeat Status
//        cart.getCartItems().forEach(item -> {
////
//            PerformanceSeat performanceSeat = item.getPerformanceSeat();
//
//            performanceSeat.setStatus(PerformanceSeatStatus.SOLD);
//
//            performanceseatRepository.save(performanceSeat);
//
//            // Clear the cart
//            cartService.deletePerformanceFromCart(cart.getCartId(), item.getPerformanceSeat().getPerformanceSeatId());
//        });
//
//      // Send back the order summary
//        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
//
//        orderDTO.setAddressId(addressId);
//
//        List<OrderItemDTO> itemDTOs = cart.getCartItems().stream()
//                .map(ci -> {
//                    OrderItemDTO dto = new OrderItemDTO();
//                    //dto.setPerformanceName(ci.getPerformanceSeat().getPerformance().getPerformanceName());
//                    dto.setRowLabel(ci.getPerformanceSeat().getSeat().getRowLabel());
//                    dto.setSection(ci.getPerformanceSeat().getSeat().getSection());
//                    dto.setOrderedPerformancePrice(ci.getPerformancePrice());
//                    dto.setDiscount(ci.getDiscount());
//                    return dto;
//                })
//                .toList();
//
//        orderDTO.setOrderItems(itemDTOs);
//
//        return orderDTO;
        // }


        {
            if ("Stripe".equalsIgnoreCase(paymentMethod)) {

                if (pgPaymentId == null || pgPaymentId.isBlank()) {
                    throw new APIException("Stripe PaymentIntent ID nije dostavljen.");
                }

                Optional<Order> existingOrder =
                        orderRepository.findByPayment_PgPaymentId(pgPaymentId);

                if (existingOrder.isPresent()) {

                    Order order = existingOrder.get();

                    if (!order.getEmail().equals(emailId)) {
                        throw new APIException(
                                "Ovo Stripe plaćanje ne pripada korisniku."
                        );
                    }

                    if ("PAID".equals(order.getOrderStatus())) {
                        return modelMapper.map(order, OrderDTO.class);
                    }

                    if ("PENDING_PAYMENT".equals(order.getOrderStatus())) {
                        return modelMapper.map(order, OrderDTO.class);
                    }

                    throw new APIException("Narudžba ima neispravan status.");
                }
            }


            // Košarica

            Cart userCart = cartRepository.findCartByEmail(emailId);

            if (userCart == null) {
                throw new ResourceNotFoundException("Cart", "email", emailId);
            }

            Cart cart = cartRepository.findByIdForUpdate(userCart.getCartId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Cart", "cartId", userCart.getCartId()
                            )
                    );

            List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());

            if (cartItems.isEmpty()) {
                throw new APIException("Košarica je prazna!");
            }

            // 1. Zaključaj i provjeri sva sjedala
            LocalDateTime now = LocalDateTime.now();

            for (CartItem item : cartItems) {

                PerformanceSeat seat = performanceseatRepository
                        .findByIdForUpdate(
                                item.getPerformanceSeat().getPerformanceSeatId()
                        )
                        .orElseThrow(() -> new APIException("Sjedalo ne postoji!"));

                if (seat.getStatus() != PerformanceSeatStatus.HELD
                        || seat.getHeldUntil() == null
                        || !seat.getHeldUntil().isAfter(now)
                        || !cart.getCartId().equals(seat.getHeldByCartId())) {

                    throw new APIException(
                            "Rezervacija je istekla. Odaberite sjedala ponovno!"
                    );
                }
            }

            // Stripe

            if ("Stripe".equalsIgnoreCase(paymentMethod)) {

                PaymentIntent intent;

                try {
                    intent = stripeService.retrievePaymentIntent(pgPaymentId);
                } catch (StripeException e) {
                    throw new APIException("Nije moguće provjeriti Stripe plaćanje.");
                }

                if (!"requires_capture".equals(intent.getStatus())) {
                    throw new APIException("Stripe plaćanje nije autorizirano.");
                }

                long expectedAmount = Math.round(cart.getTotalPrice() * 100);

                if (!Long.valueOf(expectedAmount).equals(intent.getAmount())
                        || !"eur".equalsIgnoreCase(intent.getCurrency())) {
                    throw new APIException("Iznos Stripe plaćanja nije ispravan.");
                }

                if (!cart.getCartId().toString().equals(intent.getMetadata().get("cartId"))
                        || !emailId.equals(intent.getMetadata().get("userEmail"))) {

                    throw new APIException(
                            "Stripe plaćanje ne pripada ovoj košarici."
                    );
                }
            }


            // 2. Provjeri adresu
//            Address address = addressRepository.findById(addressId)
//                    .orElseThrow(() ->
//                            new ResourceNotFoundException(
//                                    "Address", "addressId", addressId
//                            )
//                    );

            // 2. Adresa je opcionalna
            Address address = null;

            if (addressId != null) {
                address = addressRepository.findById(addressId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Address", "addressId", addressId
                                )
                        );
            }
            // 3. Kreiraj narudžbu
            Order order = new Order();
            order.setEmail(emailId);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(cart.getTotalPrice());
            order.setOrderStatus(
                    "Stripe".equalsIgnoreCase(paymentMethod)
                            ? "PENDING_PAYMENT"
                            : "Order Accepted !"
            );
            order.setAddress(address);

            Payment payment = new Payment(
                    paymentMethod,
                    pgPaymentId,
                    "Stripe".equalsIgnoreCase(paymentMethod)
                            ? "requires_capture"
                            : pgStatus,
                    pgResponseMessage,
                    pgName
            );

            payment.setOrder(order);
            payment = paymentRepository.save(payment);
            order.setPayment(payment);

            Order savedOrder = orderRepository.save(order);

            // 4. Pripremi OrderItem i OrderItemDTO
            List<OrderItem> orderItems = new ArrayList<>();
            List<OrderItemDTO> itemDTOs = new ArrayList<>();

            for (CartItem item : cartItems) {

                PerformanceSeat seat = item.getPerformanceSeat();

                OrderItem orderItem = new OrderItem();
                orderItem.setPerformanceSeat(seat);
                orderItem.setDiscount(item.getDiscount());
                orderItem.setOrderedPerformancePrice(item.getPerformancePrice());
                orderItem.setOrder(savedOrder);
                orderItems.add(orderItem);

                OrderItemDTO dto = new OrderItemDTO();
                dto.setRowLabel(seat.getSeat().getRowLabel());
                dto.setSection(seat.getSeat().getSection());
                dto.setOrderedPerformancePrice(item.getPerformancePrice());
                dto.setDiscount(item.getDiscount());
                itemDTOs.add(dto);
            }

            orderItemRepository.saveAll(orderItems);

            // 5. Označi sjedala kao SOLD i očisti košaricu
            for (CartItem item : cartItems) {

                PerformanceSeat seat = item.getPerformanceSeat();

                seat.setStatus(PerformanceSeatStatus.SOLD);
                seat.setHeldByCartId(null);
                seat.setHeldUntil(null);

                performanceseatRepository.save(seat);

                cartItemRepository.delete(item);
            }

            cart.getCartItems().clear();
            cart.setTotalPrice(0.0);

            // 6. Vrati potvrdu narudžbe
            OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
            orderDTO.setAddressId(addressId);
            orderDTO.setOrderItems(itemDTOs);

            return orderDTO;
        }
    }


    @Override
    @Transactional
    public void confirmStripePayment(String paymentIntentId) {

        Order order = orderRepository
                .findByPayment_PgPaymentId(paymentIntentId)
                .orElseThrow(() -> new APIException(
                        "Narudžba za Stripe plaćanje nije pronađena."
                ));

        // Sprječava ponovno slanje za već potvrđenu narudžbu
        if ("PAID".equals(order.getOrderStatus())) {
            return;
        }

        order.setOrderStatus("PAID");
        order.getPayment().setPgStatus("succeeded");

        orderRepository.save(order);

        // E-mail se šalje tek nakon uspješnog COMMIT-a
        sendConfirmationAfterCommit(order);
    }


    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<Order> orders = pageOrders.getContent();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());
        return orderResponse;
    }



    private void sendConfirmationAfterCommit(Order order) {

        String customerEmail = order.getEmail();
        Long orderId = order.getOrderId();
        Double totalAmount = order.getTotalAmount();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            emailService.sendOrderConfirmation(
                                    customerEmail,
                                    orderId,
                                    totalAmount
                            );
                        } catch (Exception e) {
                            System.err.println(
                                    "Greška prilikom slanja potvrde kupnje: "
                                            + e.getMessage()
                            );
                        }
                    }
                }
        );
    }


}



































