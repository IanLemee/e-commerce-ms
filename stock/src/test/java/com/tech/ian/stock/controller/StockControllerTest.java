package com.tech.ian.stock.controller;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.stock.commons.Loader;
import com.tech.ian.stock.model.dto.StockRequestDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import com.tech.ian.stock.service.StockService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = StockController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({Loader.class, com.tech.ian.exception.GlobalErrorHandler.class})
class StockControllerTest {
    private static final String URL = "/stock";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    StockService service;

    @Autowired
    private Loader loader;

    @Nested
    class create {
        @SneakyThrows
        @Test
        void shouldReturnStatus202_WhenCreateProductSuccessful() {
            var requestJson = loader.load("stock/post-stock-request-create-req-200.json");

            mockMvc.perform(MockMvcRequestBuilders
                            .post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }
    }

    @Nested

    class butProduct {
        @Test
        @SneakyThrows
        void shouldReturnStatus200_WhenBuyProductSuccessful() {
            var request = new StockResponseDto("iPhone", BigDecimal.valueOf(6000.0));
            when(service.buyProduct(any(StockRequestDto.class))).thenReturn(request);
            var requestJson = loader.load("stock/put-stock-request-buy-product-200.json");
            var responseJson = loader.load("stock/put-stock-response-buy-product-200.json");
            mockMvc.perform(MockMvcRequestBuilders
                    .put(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().json(responseJson));
        }

        @Test
        @SneakyThrows
        void shouldReturnStatus404_WhenProductNotFound() {
            when(service.buyProduct(any(StockRequestDto.class))).thenThrow(ProductOutOfStockException.class);
            var requestJson = loader.load("stock/put-stock-request-buy-product-200.json");
            mockMvc.perform(MockMvcRequestBuilders
                            .put(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                    ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }



}