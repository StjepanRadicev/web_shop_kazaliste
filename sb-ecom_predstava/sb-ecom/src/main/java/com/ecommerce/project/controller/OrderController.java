package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.payload.StripePaymentDto;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderPerformances(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        if ("Stripe".equalsIgnoreCase(paymentMethod)) {

            try {
                PaymentIntent intent = stripeService.retrievePaymentIntent(
                        orderRequestDTO.getPgPaymentId()
                );

                if ("requires_capture".equals(intent.getStatus())) {


                    System.out.println("Stripe status prije capture: " + intent.getStatus());

                    intent = stripeService.capturePaymentIntent(intent.getId());

                    System.out.println("Stripe status nakon capture: " + intent.getStatus());
                }

                if (!"succeeded".equals(intent.getStatus())) {
                    throw new APIException(
                            "Stripe plaćanje nije uspješno završeno."
                    );
                }

                orderService.confirmStripePayment(intent.getId());

            } catch (StripeException e) {
                throw new APIException(
                        "Greška prilikom Stripe naplate: " + e.getMessage()
                );
            }
        }

        return new ResponseEntity<OrderDTO>(order, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripeClientSecret(@RequestBody StripePaymentDto stripePaymentDto) throws StripeException {

        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDto);

        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }


    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(

            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PERFORMANCE_BY_01, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<OrderResponse>(orderResponse, HttpStatus.OK);
    }

}
