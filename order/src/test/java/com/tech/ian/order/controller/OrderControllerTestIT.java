package com.tech.ian.order.controller;

import com.stripe.model.PaymentIntent;
import com.tech.ian.order.commons.Loader;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.OrderItem;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.repository.OrderRepository;
import com.tech.ian.order.utils.JwtUtils;
import com.tech.ian.order.utils.StripeGateway;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@EnableWireMock(@ConfigureWireMock(port = 7070))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"stock-service.url=http://localhost:7070/stock"})
@Testcontainers
@TestPropertySource(properties = "stripe.api.key=token")
class OrderControllerTestIT {
    private static final String URL = "http://localhost";

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @LocalServerPort
    Integer port;

    @Autowired
    private OrderRepository repository;

    @Autowired
    Loader loader;

    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private StripeGateway stripeGateway;

    @BeforeEach
    void init() {
        RestAssured.baseURI = URL;
        RestAssured.port = port;
    }

    @AfterEach
    void after() {
        repository.deleteAll();
    }

    @Nested
    class createOrder {
        @Test
        @SneakyThrows
        void shouldCreateOrderWhenSuccessful() {
            stubFor(get(urlPathEqualTo("/stock/getProduct"))
                    .withQueryParam("product", equalTo("iPhone"))
                    .withQueryParam("quantity", equalTo("4"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"product\": \"iPhone\", \"price\": 1000.0}")
                            .withStatus(200)));

            PaymentIntent mockResponse = new PaymentIntent();
            mockResponse.setId("pi_123");
            mockResponse.setStatus("succeeded");

            when(stripeGateway.createPayment(any(BigDecimal.class), any(CardDetailsDto.class))).thenReturn(mockResponse);

            var card = new CardDetailsDto("pm_card_visa");
            var order = new OrderRequestDto("iPhone", 4, card);

            OrderItem item = OrderItem.builder().product(order.product()).quantity(order.quantity()).price(BigDecimal.valueOf(1000.0)).build();
            OrderEntity build = OrderEntity.builder().customerId("mail@mail.com").totalPrice(BigDecimal.valueOf(1000.0)).paymentStatus(PaymentStatus.PENDING).timestamp(LocalDateTime.now()).item(item).build();
            var entity = repository.save(build);
            var expectedResponseJson = loader.load("orderJson/post-createOrder-response-200.json");

            given()
                    .body(order)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("order/create")
                    .then()
                    .log().body()
                    .statusCode(HttpStatus.CREATED.value())
                    .body(Matchers.equalTo(expectedResponseJson));

            var foundOrderOptional = repository.findByIdAndCustomerId(entity.getId(), entity.getCustomerId()).get();
            assertNotNull(foundOrderOptional);
            assertEquals(build.getCustomerId(), foundOrderOptional.getCustomerId());
            assertEquals(build.getId(), foundOrderOptional.getId());
            assertEquals(build.getItem().getProduct(), foundOrderOptional.getItem().getProduct());
        }
    }

    @Nested
    class getProduct {
        @Test
        void shouldGetProductWhenSuccessful() {
            stubFor(get(urlPathEqualTo("/stock/getProduct"))
                    .withQueryParam("product", equalTo("iPhone"))
                    .withQueryParam("quantity", equalTo("1"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"product\": \"iPhone\", \"price\": 1000.0}")
                            .withStatus(200)));

            given()
                    .queryParam("product", "iPhone")
                    .when()
                    .get("order/info")
                    .then()
                    .statusCode(200);
        }
    }

    @Nested
    class findAll {
        @Test
        void shouldReturnAllOrders_WhenSuccessful() {
            var mail = "ianleme2@hotmail.com";
            when(jwtUtils.extractSubject(anyString())).thenReturn(mail);
            when(jwtUtils.isTokenValid(anyString())).thenReturn(true);

            var entities = List.of(
                    OrderEntity.builder().customerId(mail)
                            .totalPrice(BigDecimal.valueOf(1000.0))
                            .paymentStatus(PaymentStatus.PENDING)
                            .timestamp(LocalDateTime.now())
                            .item(new OrderItem("iPhone", 1, BigDecimal.valueOf(1000.0)))
                            .build(),
                    OrderEntity.builder()
                            .customerId(mail)
                            .totalPrice(BigDecimal.valueOf(4000.0))
                            .paymentStatus(PaymentStatus.APPROVED)
                            .timestamp(LocalDateTime.now())
                            .item(new OrderItem("MacBook", 1, BigDecimal.valueOf(4000.0)))
                            .build()
            );
            repository.saveAll(entities);

            given()
                    .header("Authorization", "Bearer " + "token")
                    .queryParam("page", 0)
                    .queryParam("size", "2")
                    .contentType(ContentType.JSON)
                    .when()
                    .get("order/orders")
                    .then()
                    .statusCode(200)
                    .log()
                    .all();
        }
    }
}