package com.tech.ian.stock.controller;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.stock.commons.Loader;
import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
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
import static org.mockito.Mockito.doNothing;
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
    class getProductInfo {
        @SneakyThrows
        @Test
        void shouldReturnStatus200_WhenRequestSuccessful() {
            var request = new StockResponseDto("iPhone", BigDecimal.valueOf(6000.0));
            doNothing().when(service).buyProduct(any(StockBuyProductDto.class));
            mockMvc.perform(MockMvcRequestBuilders
                            .get(URL + "/getProduct")
                            .param("product", "Phone")
                            .param("quantity", "3")
                    ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @SneakyThrows
        void shouldReturnStatus404_WhenProductNotFound() {
            when(service.getStockInfo(any(StockBuyProductDto.class))).thenThrow(ProductOutOfStockException.class);
            mockMvc.perform(MockMvcRequestBuilders
                            .get(URL + "/getProduct")
                            .param("product", "Phone")
                            .param("quantity", "3")
                    ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

        }
    }


}