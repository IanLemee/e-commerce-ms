package com.tech.ian.order.client;

import com.tech.ian.order.client.pojo.CardTokenResponse;
import com.tech.ian.order.model.dto.CardDetailsDto;
import lombok.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "Stripe api", url = "https://api.stripe.com/v1")
public interface StripeApiClient {

    @PostMapping("/token")
    CardTokenResponse token(@RequestHeader(name = "Authorization") String bearerToken,
            @RequestBody Map<String, String> cardParams);
}
