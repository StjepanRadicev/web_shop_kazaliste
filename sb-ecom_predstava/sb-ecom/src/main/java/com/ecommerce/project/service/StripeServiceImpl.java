package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.PerformanceSeat;
import com.ecommerce.project.model.PerformanceSeatStatus;
import com.ecommerce.project.payload.StripePaymentDto;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class StripeServiceImpl implements StripeService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

@Override
public PaymentIntent paymentIntent(StripePaymentDto stripePaymentDto) throws StripeException {



    String email = authUtil.loggedInEmail();

    Cart cart = cartRepository.findCartByEmail(email);

    if (cart == null || cart.getCartItems().isEmpty()) {
        throw new APIException("Košarica je prazna!");
    }

    LocalDateTime now = LocalDateTime.now();

    boolean invalidReservation = cart.getCartItems().stream()
            .anyMatch(item -> {
                PerformanceSeat seat = item.getPerformanceSeat();

                return seat.getStatus() != PerformanceSeatStatus.HELD
                        || seat.getHeldUntil() == null
                        || !seat.getHeldUntil().isAfter(now)
                        || !cart.getCartId().equals(seat.getHeldByCartId());
            });

    if (invalidReservation) {
        throw new APIException("Rezervacija je istekla. Odaberite sjedala ponovno.");
    }

    long amount = Math.round(cart.getTotalPrice() * 100);

    if (amount <= 0) {
        throw new APIException("Neispravan iznos plaćanja.");
    }





    PaymentIntentCreateParams params =
            PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .putMetadata("cartId", cart.getCartId().toString())
                    .putMetadata("userEmail", email)
                    .addAllowedPaymentMethodType(
                            PaymentIntentCreateParams.AllowedPaymentMethodType.CARD
                    )
                    .setCaptureMethod(
                            PaymentIntentCreateParams.CaptureMethod.MANUAL
                    )
                    .build();







    return PaymentIntent.create(params);
//    PaymentIntentCreateParams params =
//            PaymentIntentCreateParams.builder()
//                    .setAmount(stripePaymentDto.getAmount())
//                    .setCurrency(stripePaymentDto.getCurrency())
//                    .setAutomaticPaymentMethods(
//                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
//                                    .setEnabled(true)
//                                    .build()
//                    )
//                    .build();
//
//    return PaymentIntent.create(params);
}



    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId)
            throws StripeException {

        return PaymentIntent.retrieve(paymentIntentId);
    }

    @Override
    public PaymentIntent capturePaymentIntent(String paymentIntentId)
            throws StripeException {

        PaymentIntent paymentIntent =
                PaymentIntent.retrieve(paymentIntentId);

        return paymentIntent.capture();
    }

    @Override
    public PaymentIntent cancelPaymentIntent(String paymentIntentId)
            throws StripeException {

        PaymentIntent paymentIntent =
                PaymentIntent.retrieve(paymentIntentId);

        return paymentIntent.cancel();
    }

}



