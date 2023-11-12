package com.yvolabs.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yvolabs.productservice.dto.ProductRequest;
import com.yvolabs.productservice.model.Product;
import com.yvolabs.productservice.repository.ProductRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private final String PRODUCT_ENDPOINT = "/api/product";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

    }

    @BeforeEach
    public void clearData() {
        productRepository.deleteAll();

    }


    @Test
    void shouldCreateProduct() throws Exception {

        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);

        performPostRequest(productRequestString)
                .andExpect(status().isCreated());

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());
    }


    @Test
    void shouldGetProducts() throws Exception {

        performPostRequest(objectMapper.writeValueAsString(getProductRequest()));

        mockMvc.perform(MockMvcRequestBuilders.get(PRODUCT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());

    }

    @NotNull
    private ResultActions performPostRequest(String productRequestString) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(PRODUCT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productRequestString)
        );
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("Samsung Note 20")
                .description("Note 20")
                .price(BigDecimal.valueOf(1200))
                .build();
    }

}
