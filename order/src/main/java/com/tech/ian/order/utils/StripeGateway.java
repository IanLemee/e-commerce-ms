package com.tech.ian.order.utils;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.tech.ian.order.model.dto.CardDetailsDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripeGateway {

    public PaymentIntent createPayment(BigDecimal totalPrice, CardDetailsDto cardDetails) throws StripeException {
        PaymentIntentCreateParams params = buildParams(totalPrice, cardDetails);
        return PaymentIntent.create(params);
    }

    private PaymentIntentCreateParams buildParams(BigDecimal totalPrice, CardDetailsDto cardDetails) {
        return PaymentIntentCreateParams.builder()
                .setAmount(totalPrice.multiply(new BigDecimal(100)).longValue())
                .setCurrency("usd")
                .setPaymentMethod(cardDetails.paymentMethods())
                .setReturnUrl("https://www.google.com")
                .setConfirm(true)
                .build();
    }
}
