package com.tech.ian.stock.controller;

import com.tech.ian.stock.commons.Loader;
import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
import com.tech.ian.stock.repository.StockRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StockControllerTestIT {
    private static final String URL = "http://localhost";

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8.4.0"))
            .withExposedPorts(6379);
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    Integer port;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Loader loader;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = URL;
        RestAssured.port = port;
    }
    @AfterEach
    void after() {
        stockRepository.deleteAll();
        assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    class create {

        @Test
        @SneakyThrows
        void shouldCreateANewProduct_WhenSuccessful() {
            var expectedJsonResponse = loader.load("stock/post-stock-create-response-IT-200.json");
            var request = new StockCreateDto("MacBook", 2, BigDecimal.valueOf(1000.0));

            var optionalProduct = stockRepository.findByProduct(request.product());
            assertEquals(Optional.empty(), optionalProduct);

            given()
                    .body(request)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/stock")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body(Matchers.equalTo(expectedJsonResponse))
                    .log().all();

            optionalProduct = stockRepository.findByProduct(request.product());
            assertNotNull(optionalProduct);
        }

        @Test
        @SneakyThrows
        void shouldIncreaseQuantity_WhenProductExist() {
            var expectedJsonResponse = loader.load("stock/post-stock-create-response-increased-quantity-IT-200.json");

                var entity = StockEntity.builder()
                        .product("iPhone")
                        .quantity(10)
                        .price(BigDecimal.valueOf(1000.0))
                        .build();
                stockRepository.save(entity);

            var request = new StockCreateDto("iPhone", 4, BigDecimal.valueOf(1000.0));

                var optionalProduct = stockRepository.findByProduct(request.product());
                assertNotEquals(Optional.empty(), optionalProduct);


            given()
                    .body(request)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/stock")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body(Matchers.equalTo(expectedJsonResponse))
                    .log().all();

                var finalProduct = stockRepository.findByProduct("iPhone").get();
                assertEquals(14, finalProduct.getQuantity());

        }
    }

    @Nested
    class getProductInfo {
        @Test
        @SneakyThrows
        void shouldUsePostgresWhenNotCache() {
            var expectedJsonResponse = loader.load("stock/get-stock-response-IT-200.json");
            var request = new StockBuyProductDto("MacBook", 2);

                var entity = StockEntity.builder()
                        .product("MacBook")
                        .quantity(10)
                        .price(BigDecimal.valueOf(1000.0))
                        .build();
                stockRepository.save(entity);


            given()
                    .formParam("product", request.product())
                    .formParam("quantity", request.quantity())
                    .when()
                    .get("/stock/getProduct")
                    .then()
                    .statusCode(200)
                    .body(equalTo(expectedJsonResponse));
        }

        @Test
        @SneakyThrows
        void shouldUseCacheWhenStored() {
            var expectedJsonResponse = loader.load("stock/get-stock-response-IT-200.json");

            var product = StockEntity.builder().product("MacBook").quantity(2).price(BigDecimal.valueOf(1000.0)).build();
            var savedProduct = stockRepository.save(product);

            given()
                    .formParam("product", "MacBook")
                    .formParam("quantity", 1)
                    .when()
                    .get("stock/getProduct")
                    .then()
                    .statusCode(200)
                    .body(equalTo(expectedJsonResponse));

            stockRepository.delete(savedProduct);

            given()
                    .formParam("product", "MacBook")
                    .formParam("quantity", 1)
                    .when()
                    .get("stock/getProduct")
                    .then()
                    .statusCode(200)
                    .body(equalTo(expectedJsonResponse));
        }
    }
}